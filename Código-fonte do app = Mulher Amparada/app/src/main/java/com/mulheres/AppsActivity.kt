package com.mulheres

import android.graphics.Color as AndroidColor
import androidx.core.view.WindowCompat
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.drawable.toBitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


/* =========================================================
   ACTIVITY
========================================================= */

class AppsActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        /*
         * Permite que o conteúdo seja desenhado
         * por trás da barra de status.
         */
        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        /*
         * Barra de status totalmente transparente.
         */
        window.statusBarColor =
            AndroidColor.TRANSPARENT

        /*
         * Mantém os ícones da barra de status
         * claros no modo escuro.
         */
        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = false

        setContent {

            androidx.compose.runtime.CompositionLocalProvider(
                LocalOverscrollFactory provides null
            ) {

                AppsTela()
            }
        }
    }


    /* =========================================================
       FONTE
    ========================================================= */

    private val Quicksand =
        FontFamily(

            Font(
                R.font.quicksand,
                FontWeight.Normal
            ),

            Font(
                R.font.quicksand,
                FontWeight.Medium
            ),

            Font(
                R.font.quicksand,
                FontWeight.SemiBold
            ),

            Font(
                R.font.quicksand,
                FontWeight.Bold
            ),

            Font(
                R.font.quicksand,
                FontWeight.ExtraBold
            )
        )


    /* =========================================================
       CORES
    ========================================================= */

    private val FundoEscuro =
        Color(0xFF000000)

    private val FundoClaro =
        Color(0xFFF7F7FA)

    private val TextoEscuro =
        Color(0xFFF8F8FA)

    private val TextoClaro =
        Color(0xFF17171B)

    private val TextoSuaveEscuro =
        Color(0xFF6D6D76)

    private val TextoSuaveClaro =
        Color(0xFF777780)

    private val IconeFundoEscuro =
        Color.White.copy(
            alpha = 0.055f
        )

    private val IconeFundoClaro =
        Color.Black.copy(
            alpha = 0.055f
        )

    private val BarraPesquisa =
        Color(0xFFFF9191)

    private val StatusFundo =
        Color(0xFF18181A)


    /* =========================================================
       CONSTANTE
    ========================================================= */

    private const val APPS_CRIPTO_KEY =
        "apps_cache_v2"


    /* =========================================================
       MODELO
    ========================================================= */

    private data class AppItem(

        val nome: String,

        val pacote: String
    )


    /* =========================================================
       TELA
    ========================================================= */

    @Composable
    private fun AppsTela() {

        val context =
            LocalContext.current

        val packageManager =
            context.packageManager

        val dark =
            isSystemInDarkTheme()


        val fundo =
            if (dark)
                FundoEscuro
            else
                FundoClaro


        val texto =
            if (dark)
                TextoEscuro
            else
                TextoClaro


        val textoSuave =
            if (dark)
                TextoSuaveEscuro
            else
                TextoSuaveClaro


        val iconeFundo =
            if (dark)
                IconeFundoEscuro
            else
                IconeFundoClaro


        var apps by remember {

            mutableStateOf(
                emptyList<AppItem>()
            )
        }


        var pesquisa by remember {

            mutableStateOf("")
        }


        var atualizando by remember {

            mutableStateOf(false)
        }


        var status by remember {

            mutableStateOf<String?>(null)
        }


        val scope =
            rememberCoroutineScope()


        /*
         * Carrega somente o cache ao abrir.
         */

        LaunchedEffect(Unit) {

            apps =
                carregarAppsCripto(
                    context
                )
        }


        val appsFiltrados =
            remember(
                apps,
                pesquisa
            ) {

                val termo =
                    pesquisa
                        .trim()
                        .lowercase()

                if (termo.isEmpty()) {

                    apps

                } else {

                    apps.filter {

                        it.nome
                            .lowercase()
                            .contains(
                                termo
                            )
                    }
                }
            }


        fun atualizarApps() {

            if (atualizando) {
                return
            }


            scope.launch {

                atualizando =
                    true

                status =
                    "Atualizando aplicativos..."


                try {

                    val novosApps =
                        withContext(
                            Dispatchers.IO
                        ) {

                            obterAplicativos(
                                packageManager
                            )
                        }


                    if (novosApps.isEmpty()) {

                        throw Exception(
                            "Nenhum aplicativo retornado"
                        )
                    }


                    apps =
                        novosApps


                    withContext(
                        Dispatchers.IO
                    ) {

                        salvarAppsCripto(
                            context,
                            novosApps
                        )
                    }


                    status =
                        "${novosApps.size} aplicativos atualizados"


                } catch (
                    erro: Exception
                ) {

                    status =
                        if (apps.isNotEmpty()) {

                            "Não foi possível atualizar. Dados salvos mantidos."

                        } else {

                            "Não foi possível carregar os aplicativos"
                        }

                } finally {

                    atualizando =
                        false


                    delay(
                        1800
                    )


                    status =
                        null
                }
            }
        }


        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        fundo
                    )
        ) {


            /* =================================================
               GRID
            ================================================= */

            val colunas =
                if (
                    androidx.compose.ui.platform.LocalConfiguration
                        .current
                        .screenWidthDp >= 600
                ) {

                    6

                } else {

                    3
                }


            LazyVerticalGrid(

                columns =
                    GridCells.Fixed(
                        colunas
                    ),

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            top = 82.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 100.dp
                        ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        26.dp
                    ),

                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        top = 20.dp,
                        bottom = 180.dp
                    )
            ) {


                /* =============================================
                   TOPO
                ============================================= */

                item(
                    span = {
                        GridItemSpan(
                            maxLineSpan
                        )
                    }
                ) {

                    TopoApps(

                        texto =
                            texto,

                        textoSuave =
                            textoSuave,

                        atualizando =
                            atualizando,

                        onAtualizar =
                            {
                                atualizarApps()
                            }
                    )
                }


                /* =============================================
                   ESPAÇAMENTO
                ============================================= */

                item(
                    span = {
                        GridItemSpan(
                            maxLineSpan
                        )
                    }
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                2.dp
                            )
                    )
                }


                /* =============================================
                   APPS
                ============================================= */

                if (appsFiltrados.isEmpty()) {

                    item(
                        span = {
                            GridItemSpan(
                                maxLineSpan
                            )
                        }
                    ) {

                        EmptyState(

                            dark =
                                dark,

                            cacheVazio =
                                apps.isEmpty()
                        )
                    }

                } else {

                    items(

                        items =
                            appsFiltrados,

                        key = {
                            it.pacote
                        }

                    ) { app ->

                        AppItemView(

                            app =
                                app,

                            packageManager =
                                packageManager,

                            texto =
                                texto,

                            onClick = {

                                abrirApp(
                                    context,
                                    app.pacote
                                )
                            }
                        )
                    }
                }
            }


            /* =================================================
               PESQUISA
            ================================================= */

            BarraPesquisa(

                value =
                    pesquisa,

                onValueChange = {
                    pesquisa = it
                },

                dark =
                    dark,

                modifier =
                    Modifier.align(
                        Alignment.BottomCenter
                    )
            )


            /* =================================================
               STATUS
            ================================================= */

            status?.let {

                StatusAtualizacao(
                    texto = it
                )
            }
        }
    }


    /* =========================================================
       TOPO
    ========================================================= */

    @Composable
    private fun TopoApps(

        texto: Color,

        textoSuave: Color,

        atualizando: Boolean,

        onAtualizar: () -> Unit

    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(55.dp),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {


            Column(

                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(

                    text =
                        "Apps",

                    color =
                        texto,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        22.sp,

                    fontWeight =
                        FontWeight.Bold,

                    lineHeight =
                        24.sp
                )


                Text(

                    text =
                        "Aplicativos do dispositivo",

                    color =
                        textoSuave,

                    fontFamily =
                        Quicksand,

                    fontSize =
                        11.sp,

                    fontWeight =
                        FontWeight.Medium,

                    modifier =
                        Modifier.padding(
                            top = 2.dp
                        )
                )
            }


            BotaoAtualizar(

                atualizando =
                    atualizando,

                onClick =
                    onAtualizar
            )
        }
    }


    /* =========================================================
       BOTÃO ATUALIZAR
    ========================================================= */

    @Composable
    private fun BotaoAtualizar(

        atualizando: Boolean,

        onClick: () -> Unit

    ) {

        val infiniteTransition =
            rememberInfiniteTransition(
                label = "rotacao"
            )


        val rotacao by
            infiniteTransition.animateFloat(

                initialValue = 0f,

                targetValue = 360f,

                animationSpec =
                    infiniteRepeatable(

                        animation =
                            tween(
                                durationMillis = 800,
                                easing =
                                    LinearEasing
                            ),

                        repeatMode =
                            RepeatMode.Restart
                    ),

                label =
                    "rotacao"
            )


        val escala =
            if (atualizando)
                0.92f
            else
                1f


        Box(

            modifier =
                Modifier
                    .size(42.dp)
                    .scale(escala)
                    .clip(
                        CircleShape
                    )
                    .background(
                        Color.White.copy(
                            alpha = 0.055f
                        )
                    )
                    .clickable(
                        enabled =
                            !atualizando
                    ) {

                        onClick()
                    },

            contentAlignment =
                Alignment.Center
        ) {

            Icon(

                painter =
                    painterResource(
                        R.drawable.ic_refresh
                    ),

                contentDescription =
                    "Atualizar aplicativos",

                tint =
                    Color.White,

                modifier =
                    Modifier
                        .size(22.dp)
                        .then(

                            if (atualizando)
                                Modifier.rotate(
                                    rotacao
                                )
                            else
                                Modifier
                        )
            )
        }
    }


    /* =========================================================
       ITEM DO APLICATIVO
    ========================================================= */

    @Composable
    private fun AppItemView(

        app: AppItem,

        packageManager: PackageManager,

        texto: Color,

        onClick: () -> Unit

    ) {

        val icone =
            remember(
                app.pacote
            ) {

                try {

                    packageManager
                        .getApplicationIcon(
                            app.pacote
                        )

                } catch (
                    _: Exception
                ) {

                    null
                }
            }


        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            18.dp
                        )
                    )
                    .clickable {
                        onClick()
                    }
                    .padding(
                        bottom = 18.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Top
        ) {


            /* =============================================
               ÍCONE
            ============================================= */

            Box(

                modifier =
                    Modifier
                        .size(68.dp)
                        .clip(
                            RoundedCornerShape(
                                18.dp
                            )
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                if (icone != null) {

                    IconeApp(

                        drawable =
                            icone
                    )

                } else {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Gray.copy(
                                        alpha = 0.25f
                                    )
                                )
                    )
                }
            }


            /* =============================================
               NOME
            ============================================= */

            Text(

                text =
                    app.nome,

                color =
                    texto.copy(
                        alpha = 0.88f
                    ),

                fontFamily =
                    Quicksand,

                fontSize =
                    12.sp,

                fontWeight =
                    FontWeight.SemiBold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,

                modifier =
                    Modifier
                        .padding(
                            top = 8.dp
                        )
                        .width(82.dp)
            )
        }
    }


    /* =========================================================
       ÍCONE DO APP
    ========================================================= */

    @Composable
    private fun IconeApp(
        drawable: Drawable
    ) {

        val bitmap =
            remember(drawable) {

                val largura =
                    drawable.intrinsicWidth
                        .coerceAtLeast(1)

                val altura =
                    drawable.intrinsicHeight
                        .coerceAtLeast(1)

                drawable.toBitmap(
                    width = largura,
                    height = altura,
                    config = Bitmap.Config.ARGB_8888
                )
            }

        Image(

            bitmap =
                bitmap.asImageBitmap(),

            contentDescription =
                null,

            contentScale =
                ContentScale.Fit,

            modifier =
                Modifier
                    .size(68.dp)
                    .clip(
                        RoundedCornerShape(
                            17.dp
                        )
                    )
        )
    }


    /* =========================================================
       BARRA DE PESQUISA
    ========================================================= */

    @Composable
    private fun BarraPesquisa(

        value: String,

        onValueChange: (String) -> Unit,

        dark: Boolean,

        modifier: Modifier = Modifier

    ) {

        Box(

            modifier =
                modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 18.dp
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            OutlinedTextField(

                value =
                    value,

                onValueChange =
                    onValueChange,

                singleLine =
                    true,

                placeholder = {

                    Text(

                        text =
                            "Pesquisar",

                        color =
                            Color.White.copy(
                                alpha = 0.75f
                            ),

                        fontFamily =
                            Quicksand,

                        fontSize =
                            15.sp,

                        fontWeight =
                            FontWeight.SemiBold
                    )
                },

                colors =
                    TextFieldDefaults.colors(

                        focusedContainerColor =
                            BarraPesquisa,

                        unfocusedContainerColor =
                            BarraPesquisa,

                        disabledContainerColor =
                            BarraPesquisa,

                        focusedTextColor =
                            Color.White,

                        unfocusedTextColor =
                            Color.White,

                        cursorColor =
                            Color.White,

                        focusedIndicatorColor =
                            Color.Transparent,

                        unfocusedIndicatorColor =
                            Color.Transparent
                    ),

                shape =
                    RoundedCornerShape(
                        999.dp
                    ),

                modifier =
                    Modifier
                        .fillMaxWidth(
                            0.68f
                        )
                        .height(56.dp)
            )
        }
    }


    /* =========================================================
       ESTADO VAZIO
    ========================================================= */

    @Composable
    private fun EmptyState(

        dark: Boolean,

        cacheVazio: Boolean

    ) {

        val titulo =
            if (cacheVazio)
                "Nenhum aplicativo carregado"
            else
                "Nenhum aplicativo encontrado"


        val descricao =
            if (cacheVazio)
                "Toque em atualizar para carregar"
            else
                "Tente pesquisar outro nome"


        val texto =
            if (dark)
                Color.White
            else
                Color(0xFF17171B)


        val suave =
            if (dark)
                Color.White.copy(
                    alpha = 0.40f
                )
            else
                Color.Black.copy(
                    alpha = 0.45f
                )


        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 80.dp
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(

                text =
                    "◌",

                color =
                    suave,

                fontFamily =
                    Quicksand,

                fontSize =
                    34.sp
            )


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            Text(

                text =
                    titulo,

                color =
                    texto.copy(
                        alpha = 0.75f
                    ),

                fontFamily =
                    Quicksand,

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.SemiBold
            )


            Text(

                text =
                    descricao,

                color =
                    suave,

                fontFamily =
                    Quicksand,

                fontSize =
                    11.sp,

                modifier =
                    Modifier.padding(
                        top = 3.dp
                    )
            )
        }
    }


    /* =========================================================
       STATUS DE ATUALIZAÇÃO
    ========================================================= */

    @Composable
    private fun StatusAtualizacao(
        texto: String
    ) {

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),

            contentAlignment =
                Alignment.TopCenter
        ) {

            Box(

                modifier =
                    Modifier
                        .padding(
                            top = 65.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                999.dp
                            )
                        )
                        .background(
                            StatusFundo.copy(
                                alpha = 0.94f
                            )
                        )
                        .padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
            ) {

                Text(

                    text =
                        texto,

                    color =
                        Color.White.copy(
                            alpha = 0.78f
                        ),

                    fontFamily =
                        Quicksand,

                    fontSize =
                        10.sp,

                    fontWeight =
                        FontWeight.Medium
                )
            }
        }
    }


    /* =========================================================
       OBTER APLICATIVOS
    ========================================================= */

    private fun obterAplicativos(

        packageManager: PackageManager

    ): List<AppItem> {

        val aplicativos =
            packageManager
                .getInstalledApplications(
                    PackageManager.GET_META_DATA
                )


        return aplicativos

            /*
             * NÃO existe mais o filtro:
             *
             * getLaunchIntentForPackage()
             *
             * Portanto, os pacotes instalados
             * também entram na lista.
             */

            .map { app ->

                AppItem(

                    nome =
                        packageManager
                            .getApplicationLabel(
                                app
                            )
                            .toString(),

                    pacote =
                        app.packageName
                )
            }

            .distinctBy {
                it.pacote
            }

            .sortedWith(
                compareBy(
                    String.CASE_INSENSITIVE_ORDER
                ) {
                    it.nome
                }
            )
    }


    /* =========================================================
       ABRIR APLICATIVO
    ========================================================= */

    private fun abrirApp(

        context: android.content.Context,

        pacote: String

    ) {

        try {

            val intent =
                context.packageManager
                    .getLaunchIntentForPackage(
                        pacote
                    )


            if (intent != null) {

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                context.startActivity(
                    intent
                )

            } else {

                Toast
                    .makeText(
                        context,
                        "Não foi possível abrir este aplicativo",
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }

        } catch (
            _: Exception
        ) {

            Toast
                .makeText(
                    context,
                    "Não foi possível abrir este aplicativo",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }


    /* =========================================================
       CARREGAR CACHE DA CRIPTO
    ========================================================= */

    private fun carregarAppsCripto(
        context: android.content.Context
    ): List<AppItem> {

        return try {

            val cripto =
                Cripto(
                    context
                )


            val resposta =
                cripto.carregar(
                    APPS_CRIPTO_KEY
                )


            if (resposta.isNullOrEmpty()) {

                emptyList()

            } else {

                val array =
                    JSONArray(
                        resposta
                    )


                buildList {

                    for (
                        i in 0 until array.length()
                    ) {

                        val obj =
                            array.getJSONObject(
                                i
                            )


                        val nome =
                            obj.optString(
                                "nome"
                            )


                        val pacote =
                            obj.optString(
                                "pacote"
                            )


                        if (
                            nome.isNotBlank() &&
                            pacote.isNotBlank()
                        ) {

                            add(
                                AppItem(
                                    nome = nome,
                                    pacote = pacote
                                )
                            )
                        }
                    }
                }
            }

        } catch (
            _: Exception
        ) {

            emptyList()
        }
    }


    /* =========================================================
       SALVAR CACHE NA CRIPTO
    ========================================================= */

    private fun salvarAppsCripto(

        context: android.content.Context,

        apps: List<AppItem>

    ) {

        try {

            val cripto =
                Cripto(
                    context
                )


            val array =
                JSONArray()


            apps.forEach { app ->

                val obj =
                    JSONObject()


                obj.put(
                    "nome",
                    app.nome
                )


                obj.put(
                    "pacote",
                    app.pacote
                )


                array.put(
                    obj
                )
            }


            cripto.salvar(

                APPS_CRIPTO_KEY,

                array.toString()
            )

        } catch (
            _: Exception
        ) {

            /*
             * O aplicativo continua funcionando
             * mesmo se o cache não puder ser salvo.
             */
        }
    }
}