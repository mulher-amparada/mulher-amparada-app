// Copyright (C) 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package cuttlefish

import (
	"fmt"
	"strings"

	"github.com/google/blueprint"
	"github.com/google/blueprint/proptools"

	"android/soong/android"
	"android/soong/cc"
)

//go:generate go run ../../../../build/blueprint/gobtools/codegen

var pctx = android.NewPackageContext("android/soong/cuttlefish")

func init() {
	pctx.Import("android/soong/android")
	android.RegisterModuleType("cvd_host_package", cvdHostPackageFactory)
	android.RegisterParallelSingletonType("cvd_host_package_singleton", cvdHostPackageSingletonFactory)
}

type cvdHostPackage struct {
	android.ModuleBase
	android.PackagingBase
	blueprint.ModuleUsesIncrementalWalkDeps
}

// We need to implement IsNativeCoverageNeeded so that in coverage builds we don't get packaging
// conflicts with required deps that always use the coverage variant.
func (p *cvdHostPackage) IsNativeCoverageNeeded(ctx cc.IsNativeCoverageNeededContext) bool {
	return ctx.DeviceConfig().NativeCoverageEnabled()
}

var _ cc.UseCoverage = (*cvdHostPackage)(nil)

func cvdHostPackageFactory() android.Module {
	module := &cvdHostPackage{}
	android.InitPackageModule(module)
	android.InitAndroidArchModule(module, android.HostSupported, android.MultilibFirst)
	module.IgnoreMissingDependencies = true
	return module
}

type dependencyTag struct {
	blueprint.BaseDependencyTag
	android.InstallAlwaysNeededDependencyTag // to force installation of both "deps" and manually added dependencies
	android.PackagingItemAlwaysDepTag        // to force packaging of both "deps" and manually added dependencies
}

var cvdHostPackageDependencyTag = dependencyTag{}

func (c *cvdHostPackage) DepsMutator(ctx android.BottomUpMutatorContext) {
	c.AddDeps(ctx, cvdHostPackageDependencyTag)

	variations := []blueprint.Variation{
		{Mutator: "os", Variation: ctx.Target().Os.String()},
		{Mutator: "arch", Variation: android.Common.String()},
	}
	for _, dep := range strings.Split(
		ctx.Config().VendorConfig("cvd").String("grub_config"), " ") {
		if ctx.OtherModuleExists(dep) {
			ctx.AddVariationDependencies(variations, cvdHostPackageDependencyTag, dep)
		}
	}
	for _, dep := range strings.Split(
		ctx.Config().VendorConfig("cvd").String("launch_configs"), " ") {
		if ctx.OtherModuleExists(dep) {
			ctx.AddVariationDependencies(variations, cvdHostPackageDependencyTag, dep)
		}
	}

	for _, dep := range strings.Split(
		ctx.Config().VendorConfig("cvd").String("binary"), " ") {
		if ctx.OtherModuleExists(dep) {
			ctx.AddVariationDependencies(ctx.Target().Variations(), cvdHostPackageDependencyTag, dep)
		}
	}

	// If cvd_custom_action_config is set, include custom action servers in the
	// host package as specified by cvd_custom_action_servers.
	customActionConfig := ctx.Config().VendorConfig("cvd").String("custom_action_config")
	if customActionConfig != "" && ctx.OtherModuleExists(customActionConfig) {
		ctx.AddVariationDependencies(variations, cvdHostPackageDependencyTag,
			customActionConfig)
		for _, dep := range strings.Split(
			ctx.Config().VendorConfig("cvd").String("custom_action_servers"), " ") {
			if ctx.OtherModuleExists(dep) {
				ctx.AddVariationDependencies(nil, cvdHostPackageDependencyTag, dep)
			}
		}
	}

	// Include custom CSS file in host package if custom_style is set
	custom_style := ctx.Config().VendorConfig("cvd").String("custom_style")
	if custom_style == "" || !ctx.OtherModuleExists(custom_style) {
		custom_style = "webrtc_custom_blank.css"
	}
	ctx.AddVariationDependencies(variations, cvdHostPackageDependencyTag, custom_style)
}

func (c *cvdHostPackage) GenerateAndroidBuildActions(ctx android.ModuleContext) {
	sboxDir := android.PathForModuleOut(ctx, "sbox")
	sboxManifest := android.PathForModuleOut(ctx, "sbox.manifest")
	packageDir := sboxDir.Join(ctx, "staging_dir")
	tarball := sboxDir.Join(ctx, c.BaseModuleName()+".tar.gz")

	builder := android.NewRuleBuilder(pctx, ctx).
		Sbox(sboxDir, sboxManifest).
		SandboxDisabled()
	builder.Command().Text("rm").Flag("-rf").Text(packageDir.String())
	builder.Command().Text("mkdir").Flag("-p").Text(packageDir.String())
	specs := c.GatherPackagingSpecs(ctx)
	c.CopySpecsToDir(ctx, builder, specs, packageDir)

	builder.Command().Text("tar Scfz").
		Output(tarball).
		Flag("-C").
		Text(packageDir.String()).
		Flag("--mtime='2020-01-01'"). // to have reproducible builds
		Text(".")
	builder.Build("cvd_host_tarball", fmt.Sprintf("Creating tarball for %s", c.BaseModuleName()))

	// We install the tarball to out/host/linux-x86 for two reasons:
	// - acloud looks for it in this location:
	//   https://cs.android.com/android/platform/superproject/main/+/main:tools/acloud/create/create_common.py;l=156;drc=0096a611441f708ad97a8b9935a7dc9a8d22d2b7
	//
	// - The inputs to the tarball need to be copied to their installed locations for `launch_cvd`
	//   to work. This takes advantage of a very subtle behavior in soong:
	//   installed files also depend on all of the current module's transitive installed files.
	//   So installing the tarball also causes all of the tarball's inputs to be installed.
	installedTarball := ctx.InstallFile(android.PathForModuleInstall(ctx), c.BaseModuleName()+".tar.gz", tarball)

	android.SetProvider(ctx, CvdHostPackageMetadataInfoProvider, CvdHostPackageMetadataInfo{
		TarballMetadata: installedTarball,
		IsLinuxX8664: ctx.Os().Linux() && ctx.Arch().ArchType == android.X86_64,
	})

	ctx.ModulePhonyFiles(tarball)
}

// @auto-generate: gob
type CvdHostPackageMetadataInfo struct {
	TarballMetadata android.Path
	IsLinuxX8664 bool
}
var CvdHostPackageMetadataInfoProvider = blueprint.NewProvider[CvdHostPackageMetadataInfo]()

type cvdHostPackageSingleton struct {}

func cvdHostPackageSingletonFactory() android.Singleton {
	return &cvdHostPackageSingleton{}
}

// Create "hosttar" phony target with "cvd-host_package.tar.gz" path.
func (p *cvdHostPackageSingleton) GenerateBuildActions(ctx android.SingletonContext) {
	var cvdHostPackageMetadata []CvdHostPackageMetadataInfo

	ctx.VisitAllModuleProxies(func(module android.ModuleProxy) {
		if !android.OtherModulePointerProviderOrDefault(ctx, module, android.CommonModuleInfoProvider).Enabled {
			return
		}
		if c, ok := android.OtherModuleProvider(ctx, module, CvdHostPackageMetadataInfoProvider); ok {
			if !android.IsModulePreferredProxy(ctx, module) {
				return
			}
			cvdHostPackageMetadata = append(cvdHostPackageMetadata, c)
		}
	})

	board_platform := proptools.String(ctx.Config().ProductVariables().BoardPlatform)
	if (board_platform == "vsoc_arm") || (board_platform == "vsoc_arm64") || (board_platform == "vsoc_riscv64") || (board_platform == "vsoc_x86") || (board_platform == "vsoc_x86_64") {
		for _, info := range cvdHostPackageMetadata {
			ctx.Phony("hosttar", info.TarballMetadata)
			ctx.Phony("droidcore", info.TarballMetadata)
			// The riscv64 cuttlefish builds can be run on qemu on an x86_64 or arm64 host. Dist both sets of host packages.
			if len(cvdHostPackageMetadata) > 1 && info.IsLinuxX8664 {
				ctx.DistForGoalWithFilename("dist_files", info.TarballMetadata, "cvd-host_package-x86_64.tar.gz")
			} else {
				ctx.DistForGoal("dist_files", info.TarballMetadata)
			}
		}
	}
}
