/* Runtime de referência AMPAW01: forward pass Transformer causal, batch=1. */
#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct { char name[160]; uint32_t rank, dims[4]; size_t count; float *v; } Tensor;
static Tensor ts[256]; static uint32_t nt, vocab, block, emb, heads, layers;
static int read_u16(FILE*f,uint16_t*x){return fread(x,2,1,f)==1;}
static int read_u32(FILE*f,uint32_t*x){return fread(x,4,1,f)==1;}
static Tensor* get(const char*n){for(uint32_t i=0;i<nt;i++)if(!strcmp(ts[i].name,n))return &ts[i];return NULL;}
static int load(const char*path){
 FILE*f=fopen(path,"rb"); if(!f){perror(path);return 0;} char magic[8];
 if(fread(magic,1,8,f)!=8||memcmp(magic,"AMPAW01\0",8)){fprintf(stderr,"formato invalido\n");fclose(f);return 0;}
 uint32_t count;
 if(!read_u32(f,&vocab)||!read_u32(f,&block)||!read_u32(f,&emb)||!read_u32(f,&heads)||!read_u32(f,&layers)||!read_u32(f,&count)||count>256){fclose(f);return 0;}
 nt=count;
 for(uint32_t i=0;i<nt;i++){
  uint16_t n; if(!read_u16(f,&n)||n>=160){fclose(f);return 0;}
  if(fread(ts[i].name,1,n,f)!=n){fclose(f);return 0;} ts[i].name[n]=0;
  unsigned char rank;if(fread(&rank,1,1,f)!=1||rank>4){fclose(f);return 0;}
  ts[i].rank=rank;ts[i].count=1;
  for(unsigned j=0;j<rank;j++){if(!read_u32(f,&ts[i].dims[j])){fclose(f);return 0;}ts[i].count*=ts[i].dims[j];}
  ts[i].v=malloc(ts[i].count*sizeof(float));if(!ts[i].v||fread(ts[i].v,sizeof(float),ts[i].count,f)!=ts[i].count){fclose(f);return 0;}
 }
 fclose(f);return 1;
}
static void linear(const float*x,const float*w,const float*b,float*y,int in,int out){
 for(int j=0;j<out;j++){float s=b?b[j]:0;for(int i=0;i<in;i++)s+=x[i]*w[j*in+i];y[j]=s;}
}
static void norm(float*x,const float*g,const float*b,int n){
 float mean=0,var=0;for(int i=0;i<n;i++)mean+=x[i];mean/=n;
 for(int i=0;i<n;i++){float d=x[i]-mean;var+=d*d;}var/=n;
 float inv=1.0f/sqrtf(var+1e-5f);for(int i=0;i<n;i++)x[i]=(x[i]-mean)*inv*g[i]+b[i];
}
static float gelu(float x){return 0.5f*x*(1.0f+erff(x*0.7071067811865475f));}
static int forward(const int*ids,int n,float*out){
 if(n<1||n>block||emb%heads||emb>2048||4*emb>8192||n>2048)return 0;
 Tensor *te=get("token_embedding.weight"),*pe=get("position_embedding.weight"),*fg=get("final_norm.weight"),*fb=get("final_norm.bias"),*hw=get("language_head.weight");
 if(!te||!pe||!fg||!fb||!hw)return 0;
 float*x=calloc((size_t)n*emb,sizeof(float));if(!x)return 0;
 for(int t=0;t<n;t++){if(ids[t]<0||ids[t]>=(int)vocab){free(x);return 0;}
  for(uint32_t d=0;d<emb;d++)x[t*emb+d]=te->v[ids[t]*emb+d]+pe->v[t*emb+d];}
 int hd=emb/heads,ff=4*emb;
 for(uint32_t l=0;l<layers;l++){
  char key[160];Tensor *n1g,*n1b,*iw,*ib,*ow,*ob,*n2g,*n2b,*w1,*b1,*w2,*b2;
  #define FIND(var,fmt) do{snprintf(key,sizeof(key),fmt,l);var=get(key);if(!var){free(x);return 0;}}while(0)
  FIND(n1g,"blocks.%u.norm1.weight");FIND(n1b,"blocks.%u.norm1.bias");
  FIND(iw,"blocks.%u.attention.in_proj_weight");FIND(ib,"blocks.%u.attention.in_proj_bias");
  FIND(ow,"blocks.%u.attention.out_proj.weight");FIND(ob,"blocks.%u.attention.out_proj.bias");
  FIND(n2g,"blocks.%u.norm2.weight");FIND(n2b,"blocks.%u.norm2.bias");
  FIND(w1,"blocks.%u.feed_forward.0.weight");FIND(b1,"blocks.%u.feed_forward.0.bias");
  FIND(w2,"blocks.%u.feed_forward.2.weight");FIND(b2,"blocks.%u.feed_forward.2.bias");
  float*att=calloc((size_t)n*emb,sizeof(float));float*qkv=calloc((size_t)3*emb,sizeof(float));
  float*qn=calloc((size_t)n*emb,sizeof(float));float*ctx=calloc((size_t)n*emb,sizeof(float));
  if(!att||!qkv||!qn||!ctx){free(x);free(att);free(qkv);free(qn);free(ctx);return 0;}
  for(int t=0;t<n;t++){
   float normalized[2048]; if(emb>2048){free(x);free(att);free(qkv);free(qn);free(ctx);return 0;}
   memcpy(normalized,x+t*emb,emb*sizeof(float)); norm(normalized,n1g->v,n1b->v,emb);
   linear(normalized,iw->v,ib->v,qkv,emb,3*emb);
   memcpy(qn+t*emb,qkv,emb*sizeof(float));
   /* K e V são calculados por posição abaixo e guardados em ctx/att temporários. */
   memcpy(att+t*emb,qkv+emb,emb*sizeof(float));
   memcpy(ctx+t*emb,qkv+2*emb,emb*sizeof(float));
  }
  float*joined=calloc((size_t)n*emb,sizeof(float));if(!joined){free(x);free(att);free(qkv);free(qn);free(ctx);return 0;}
  for(int t=0;t<n;t++)for(uint32_t h=0;h<heads;h++){
   float scores[2048];if(n>2048){free(x);free(att);free(qkv);free(qn);free(ctx);free(joined);return 0;}
   float mx=-1e30f;
   for(int s=0;s<=t;s++){float dot=0;for(int d=0;d<hd;d++)dot+=qn[t*emb+h*hd+d]*att[s*emb+h*hd+d];scores[s]=dot/sqrtf((float)hd);if(scores[s]>mx)mx=scores[s];}
   float sum=0;for(int s=0;s<=t;s++){scores[s]=expf(scores[s]-mx);sum+=scores[s];}
   for(int d=0;d<hd;d++){float z=0;for(int s=0;s<=t;s++)z+=(scores[s]/sum)*ctx[s*emb+h*hd+d];joined[t*emb+h*hd+d]=z;}
  }
  for(int t=0;t<n;t++){float proj[2048];if(emb>2048){free(x);return 0;}linear(joined+t*emb,ow->v,ob->v,proj,emb,emb);for(uint32_t d=0;d<emb;d++)x[t*emb+d]+=proj[d];}
  for(int t=0;t<n;t++){float normalized[2048],hidden[8192],outv[2048];
   if(ff>8192||emb>2048){free(x);return 0;}memcpy(normalized,x+t*emb,emb*sizeof(float));norm(normalized,n2g->v,n2b->v,emb);linear(normalized,w1->v,b1->v,hidden,emb,ff);for(int j=0;j<ff;j++)hidden[j]=gelu(hidden[j]);linear(hidden,w2->v,b2->v,outv,ff,emb);for(uint32_t d=0;d<emb;d++)row[d]+=outv[d];}
  free(att);free(qkv);free(qn);free(ctx);free(joined);
 }
 float*last=x+(n-1)*emb;norm(last,fg->v,fb->v,emb);linear(last,hw->v,NULL,out,emb,vocab);free(x);return 1;
}
int main(int argc,char**argv){
 if(argc<3){fprintf(stderr,"uso: %s pesos.ampaw token_id [token_id ...]\n",argv[0]);return 2;}
 if(!load(argv[1])){fprintf(stderr,"falha ao ler pesos\n");return 1;}
 int n=argc-2,*ids=malloc((size_t)n*sizeof(int));for(int i=0;i<n;i++)ids[i]=atoi(argv[i+2]);
 float*logits=malloc((size_t)vocab*sizeof(float));if(!ids||!logits||!forward(ids,n,logits)){fprintf(stderr,"forward falhou (IDs, dimensoes ou tensores incompatíveis)\n");return 1;}
 int best=0;for(uint32_t i=1;i<vocab;i++)if(logits[i]>logits[best])best=(int)i;
 printf("next_token_id=%d\n",best);for(uint32_t i=0;i<vocab;i++)printf("%s%.7g",i?",":"logits=",logits[i]);printf("\n");
 for(uint32_t i=0;i<nt;i++)free(ts[i].v);free(ids);free(logits);return 0;
}
