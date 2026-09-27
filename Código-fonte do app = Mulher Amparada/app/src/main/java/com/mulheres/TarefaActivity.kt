package com.mulheres

import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.imePadding
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import androidx.activity.enableEdgeToEdge


/* =========================================================
   FONTE
========================================================= */

private val Quicksand = FontFamily(
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.SemiBold
    ),
    Font(
        resId = R.font.quicksand,
        weight = FontWeight.Bold
    )
)


/* =========================================================
   CORES
========================================================= */

private val DarkBackground = Color(0xFF070709)
private val DarkCard = Color(0x0EFFFFFF)
private val DarkBorder = Color(0x16FFFFFF)
private val DarkText = Color(0xFFF5F5F7)
private val DarkMuted = Color(0x73FFFFFF)

private val LightBackground = Color(0xFFF6F6F8)
private val LightCard = Color(0xFFFFFFFF)
private val LightBorder = Color(0x18000000)
private val LightText = Color(0xFF151519)
private val LightMuted = Color(0x88000000)

private val Accent = Color(0xFFFF9DB7)
private val AccentSoft = Color(0x24FF9DB7)

private val Danger = Color(0xFFFF6878)
private val Success = Color(0xFF6DFFAD)
private val Warning = Color(0xFFFFD76A)

private val HighColor = Color(0xFFFF5D68)
private val MediumColor = Color(0xFFFFD75C)
private val LowColor = Color(0xFF6CFF9F)


/* =========================================================
   MODELO
========================================================= */

data class Tarefa(
    val text: String,
    val cat: String,
    val priority: String,
    val done: Boolean,
    val created: Long,
    val updated: Long
)


/* =========================================================
   ATIVIDADE
========================================================= */

class TarefaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        /*
         * Impede capturas de tela.
         */
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE
        )

        /*
         * Não usa edge-to-edge.
         * O conteúdo fica separado das duas barras do sistema.
         */
        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
        )

        setContent {

    val dark =
        androidx.compose.foundation.isSystemInDarkTheme()

    window.statusBarColor =
        if (dark) {
            AndroidColor.BLACK
        } else {
            AndroidColor.WHITE
        }

    window.navigationBarColor =
        if (dark) {
            AndroidColor.BLACK
        } else {
            AndroidColor.WHITE
        }

    WindowCompat.getInsetsController(
        window,
        window.decorView
    ).isAppearanceLightStatusBars = !dark

    WindowCompat.getInsetsController(
        window,
        window.decorView
    ).isAppearanceLightNavigationBars = !dark

    TarefaScreen()
}
        }
    }


/* =========================================================
   TELA
========================================================= */

@Composable
private fun TarefaScreen() {

    val context = LocalContext.current

    val dark =
        androidx.compose.foundation.isSystemInDarkTheme()

    val background =
        if (dark) DarkBackground else LightBackground

    val card =
        if (dark) DarkCard else LightCard

    val border =
        if (dark) DarkBorder else LightBorder

    val textColor =
        if (dark) DarkText else LightText

    val muted =
        if (dark) DarkMuted else LightMuted

    var tasks by remember {
        mutableStateOf(
            loadTasks(context)
        )
    }

    var currentFilter by rememberSaveable {
        mutableStateOf("Todos")
    }

    var search by rememberSaveable {
        mutableStateOf("")
    }

    var currentSort by rememberSaveable {
        mutableStateOf("default")
    }

    var showTaskDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var editingIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }

    var toast by remember {
        mutableStateOf<String?>(null)
    }

    var pomodoroTime by rememberSaveable {
        mutableIntStateOf(25 * 60)
    }

    var pomodoroRunning by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(pomodoroRunning) {

        while (pomodoroRunning) {

            delay(1000)

            if (pomodoroTime > 0) {

                pomodoroTime--

            } else {

                pomodoroRunning = false
                pomodoroTime = 25 * 60
                toast = "Pomodoro finalizado!"
            }
        }
    }

    LaunchedEffect(toast) {

        if (toast != null) {

            delay(2200)

            toast = null
        }
    }

    val visibleTasks =
        remember(
            tasks,
            currentFilter,
            search,
            currentSort
        ) {

            getVisibleTasks(
                tasks = tasks,
                filter = currentFilter,
                search = search,
                sort = currentSort
            )
        }

    val pending =
        tasks.count { !it.done }

    val done =
        tasks.count { it.done }

    val subtitle =
        if (pending > 0) {
            "$pending tarefa${if (pending == 1) "" else "s"} pendente${if (pending == 1) "" else "s"}"
        } else {
            "Tudo em dia"
        }

    Scaffold(
        containerColor = background,

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    editingIndex = -1
                    showTaskDialog = true
                },
                modifier = Modifier
                    .padding(
                        end = 8.dp,
                        bottom = 8.dp
                    )
                    .size(58.dp),
                shape = RoundedCornerShape(20.dp),
                containerColor = Accent,
                contentColor = Color.Black
            ) {

                Text(
                    text = "+",
                    fontFamily = Quicksand,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .imePadding()
                    .padding(
                        start = 13.dp,
                        end = 13.dp,
                        top = 8.dp,
                        bottom = 110.dp
                    )
            ) {

                Header(
                    subtitle = subtitle,
                    textColor = textColor,
                    muted = muted
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Stats(
                    total = tasks.size,
                    pending = pending,
                    done = done,
                    card = card,
                    border = border,
                    textColor = textColor,
                    muted = muted
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                SearchBox(
                    value = search,
                    onValueChange = {
                        search = it
                    },
                    dark = dark,
                    border = border,
                    muted = muted
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Filters(
                    current = currentFilter,
                    onChange = {
                        currentFilter = it
                    },
                    dark = dark
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Toolbar(
                    count = visibleTasks.size,
                    currentSort = currentSort,
                    onSortChange = {
                        currentSort = it
                    },
                    dark = dark,
                    muted = muted,
                    border = border
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                if (visibleTasks.isEmpty()) {

                    EmptyState(
                        hasTasks = tasks.isNotEmpty(),
                        textColor = textColor,
                        muted = muted
                    )

                } else {

                    visibleTasks.forEach { task ->

                        val realIndex =
                            tasks.indexOf(task)

                        TaskCard(
                            task = task,
                            dark = dark,
                            card = card,
                            border = border,
                            textColor = textColor,
                            muted = muted,

                            onToggle = {

                                val newList =
                                    tasks.toMutableList()

                                val old =
                                    newList[realIndex]

                                newList[realIndex] =
                                    old.copy(
                                        done = !old.done,
                                        updated = System.currentTimeMillis()
                                    )

                                tasks = newList

                                saveTasks(
                                    context,
                                    tasks
                                )

                                toast =
                                    if (newList[realIndex].done) {
                                        "Tarefa concluída."
                                    } else {
                                        "Tarefa reaberta."
                                    }
                            },

                            onEdit = {

                                editingIndex =
                                    realIndex

                                showTaskDialog = true
                            },

                            onDelete = {

                                val removed =
                                    tasks[realIndex]

                                tasks =
                                    tasks
                                        .toMutableList()
                                        .also {
                                            it.removeAt(realIndex)
                                        }

                                saveTasks(
                                    context,
                                    tasks
                                )

                                toast =
                                    "\"${removed.text}\" removida."
                            }
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }
                }

                Pomodoro(
                    time = pomodoroTime,
                    running = pomodoroRunning,
                    onToggle = {

                        if (pomodoroTime <= 0) {
                            pomodoroTime = 25 * 60
                        }

                        pomodoroRunning =
                            !pomodoroRunning
                    },
                    onReset = {

                        pomodoroRunning = false
                        pomodoroTime = 25 * 60
                    },
                    onAddMinute = {

                        pomodoroTime =
                            minOf(
                                25 * 60,
                                pomodoroTime + 60
                            )
                    },
                    dark = dark,
                    card = card,
                    border = border,
                    muted = muted
                )
            }

            toast?.let {

                ToastMessage(
                    message = it,
                    dark = dark,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            bottom = 85.dp
                        )
                )
            }
        }
    }

    if (showTaskDialog) {

        TaskDialog(
            task =
                if (
                    editingIndex >= 0 &&
                    editingIndex < tasks.size
                ) {
                    tasks[editingIndex]
                } else {
                    null
                },

            dark = dark,

            onDismiss = {
                showTaskDialog = false
                editingIndex = -1
            },

            onSave = { text, category, priority ->

                if (editingIndex >= 0) {

                    val list =
                        tasks.toMutableList()

                    val old =
                        list[editingIndex]

                    list[editingIndex] =
                        old.copy(
                            text = text,
                            cat = category,
                            priority = priority,
                            updated =
                                System.currentTimeMillis()
                        )

                    tasks = list

                    saveTasks(
                        context,
                        tasks
                    )

                    toast = "Tarefa atualizada."

                } else {

                    val now =
                        System.currentTimeMillis()

                    tasks =
                        listOf(
                            Tarefa(
                                text = text,
                                cat = category,
                                priority = priority,
                                done = false,
                                created = now,
                                updated = now
                            )
                        ) + tasks

                    saveTasks(
                        context,
                        tasks
                    )

                    toast = "Tarefa adicionada."
                }

                showTaskDialog = false
                editingIndex = -1
            }
        )
    }
}


/* =========================================================
   HEADER
========================================================= */

@Composable
private fun Header(
    subtitle: String,
    textColor: Color,
    muted: Color
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 5.dp,
                end = 5.dp,
                top = 5.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(43.dp)
                .clip(
                    RoundedCornerShape(15.dp)
                )
                .background(
                    AccentSoft
                )
                .border(
                    1.dp,
                    Accent.copy(alpha = .24f),
                    RoundedCornerShape(15.dp)
                ),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.ic_check
                ),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                colorFilter =
                    ColorFilter.tint(Accent)
            )
        }

        Spacer(
            modifier = Modifier.width(11.dp)
        )

        Column {

            Text(
                text = "Planner",
                color = textColor,
                fontFamily = Quicksand,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = muted,
                fontFamily = Quicksand,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


/* =========================================================
   ESTATÍSTICAS
========================================================= */

@Composable
private fun Stats(
    total: Int,
    pending: Int,
    done: Int,
    card: Color,
    border: Color,
    textColor: Color,
    muted: Color
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {

        StatCard(
            number = total,
            label = "Total",
            card = card,
            border = border,
            muted = muted,
            modifier =
                Modifier.weight(1f)
        )

        StatCard(
            number = pending,
            label = "Pendentes",
            card = card,
            border = border,
            muted = muted,
            modifier =
                Modifier.weight(1f)
        )

        StatCard(
            number = done,
            label = "Concluídas",
            card = card,
            border = border,
            muted = muted,
            modifier =
                Modifier.weight(1f)
        )
    }
}


@Composable
private fun StatCard(
    number: Int,
    label: String,
    card: Color,
    border: Color,
    muted: Color,
    modifier: Modifier
) {

    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(card)
            .border(
                1.dp,
                border,
                RoundedCornerShape(18.dp)
            )
            .padding(11.dp)
    ) {

        Text(
            text = number.toString(),
            color = Accent,
            fontFamily = Quicksand,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color = muted,
            fontFamily = Quicksand,
            fontSize = 10.sp
        )
    }
}


/* =========================================================
   PESQUISA
========================================================= */

@Composable
private fun SearchBox(
    value: String,
    onValueChange: (String) -> Unit,
    dark: Boolean,
    border: Color,
    muted: Color
) {

    val background =
        if (dark) {
            Color(0x0EFFFFFF)
        } else {
            Color.White
        }

    TextField(
        value = value,
        onValueChange = onValueChange,

        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),

        placeholder = {

            Text(
                "Pesquisar tarefas...",
                color = muted,
                fontFamily = Quicksand,
                fontSize = 13.sp
            )
        },

        leadingIcon = {

            Image(
                painter = painterResource(
                    id = R.drawable.ic_search
                ),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter =
                    ColorFilter.tint(muted)
            )
        },

        singleLine = true,

        textStyle = TextStyle(
            color =
                if (dark) Color.White
                else LightText,
            fontFamily = Quicksand,
            fontSize = 13.sp
        ),

        keyboardOptions =
            KeyboardOptions(
                imeAction = ImeAction.Search
            ),

        colors = TextFieldDefaults.colors(

            focusedContainerColor =
                background,

            unfocusedContainerColor =
                background,

            disabledContainerColor =
                background,

            focusedIndicatorColor =
                Color.Transparent,

            unfocusedIndicatorColor =
                Color.Transparent,

            cursorColor =
                Accent
        ),

        shape =
            RoundedCornerShape(17.dp)
    )
}


/* =========================================================
   FILTROS
========================================================= */

@Composable
private fun Filters(
    current: String,
    onChange: (String) -> Unit,
    dark: Boolean
) {

    val scroll =
        rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {

        listOf(
            "Todos" to "Todas",
            "Estudos" to "Estudos",
            "Trabalho" to "Trabalho",
            "Pessoal" to "Pessoal",
            "Saúde" to "Saúde"
        ).forEach { (value, label) ->

            val active =
                current == value

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            Accent
                        } else if (dark) {
                            Color(0x0AFFFFFF)
                        } else {
                            Color.White
                        }
                    )
                    .border(
                        1.dp,
                        if (active) {
                            Accent
                        } else {
                            if (dark) {
                                DarkBorder
                            } else {
                                LightBorder
                            }
                        },
                        CircleShape
                    )
                    .clickable {
                        onChange(value)
                    }
                    .padding(
                        horizontal = 13.dp,
                        vertical = 8.dp
                    )
            ) {

                Text(
                    text = label,
                    color =
                        if (active) {
                            Color.Black
                        } else if (dark) {
                            Color.White.copy(.58f)
                        } else {
                            LightText.copy(.65f)
                        },
                    fontFamily = Quicksand,
                    fontSize = 11.sp,
                    fontWeight =
                        if (active) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        }
                )
            }
        }
    }
}


/* =========================================================
   TOOLBAR
========================================================= */

@Composable
private fun Toolbar(
    count: Int,
    currentSort: String,
    onSortChange: (String) -> Unit,
    dark: Boolean,
    muted: Color,
    border: Color
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 5.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text =
                "$count ${
                    if (count == 1)
                        "tarefa"
                    else
                        "tarefas"
                }",
            color = muted,
            fontFamily = Quicksand,
            fontSize = 10.sp
        )

        SortDropdown(
            current = currentSort,
            onChange = onSortChange,
            dark = dark,
            border = border
        )
    }
}


/* =========================================================
   ORDENAÇÃO
========================================================= */

@Composable
private fun SortDropdown(
    current: String,
    onChange: (String) -> Unit,
    dark: Boolean,
    border: Color
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    val labels =
        mapOf(
            "default" to "Ordem padrão",
            "priority" to "Prioridade",
            "name" to "Nome",
            "status" to "Pendentes primeiro"
        )

    Box {

        Row(
            modifier = Modifier
                .widthIn(min = 145.dp)
                .height(34.dp)
                .clip(
                    RoundedCornerShape(11.dp)
                )
                .background(
                    if (dark) {
                        Color(0x0DFFFFFF)
                    } else {
                        Color.White
                    }
                )
                .border(
                    1.dp,
                    if (expanded) {
                        Accent.copy(.30f)
                    } else {
                        border
                    },
                    RoundedCornerShape(11.dp)
                )
                .clickable {
                    expanded = !expanded
                }
                .padding(
                    horizontal = 12.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text =
                    labels[current] ?: "Ordem padrão",
                color =
                    if (dark) Color.White
                    else LightText,
                fontFamily = Quicksand,
                fontSize = 10.sp
            )

            Image(
                painter = painterResource(
                    id = R.drawable.ic_arrow_down
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .rotate(
                        if (expanded) 180f else 0f
                    ),
                colorFilter =
                    ColorFilter.tint(
                        if (expanded) Accent
                        else if (dark)
                            Color.White.copy(.55f)
                        else
                            Color.Black.copy(.55f)
                    )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            labels.forEach { (value, label) ->

                DropdownMenuItem(
                    text = {

                        Text(
                            text = label,
                            fontFamily = Quicksand,
                            fontSize = 11.sp,
                            color =
                                if (value == current) {
                                    Accent
                                } else {
                                    if (dark)
                                        Color.White
                                    else
                                        LightText
                                },
                            fontWeight =
                                if (value == current)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal
                        )
                    },

                    onClick = {

                        onChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}


/* =========================================================
   TAREFA
========================================================= */

@Composable
private fun TaskCard(
    task: Tarefa,
    dark: Boolean,
    card: Color,
    border: Color,
    textColor: Color,
    muted: Color,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val priorityColor =
        when (task.priority) {
            "high" -> HighColor
            "medium" -> MediumColor
            else -> LowColor
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(
                if (task.done) .46f else 1f
            )
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(card)
            .border(
                BorderStroke(
                    1.dp,
                    border
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(
                horizontal = 9.dp,
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .width(3.dp)
                .height(45.dp)
                .clip(
                    RoundedCornerShape(3.dp)
                )
                .background(priorityColor)
        )

        Spacer(
            modifier = Modifier.width(9.dp)
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(
                    RoundedCornerShape(12.dp)
                )
                .background(
                    if (task.done)
                        Success
                    else if (dark)
                        Color(0x0AFFFFFF)
                    else
                        Color(0x08000000)
                )
                .border(
                    1.dp,
                    if (task.done)
                        Success
                    else
                        border,
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    onToggle()
                },
            contentAlignment =
                Alignment.Center
        ) {

            if (task.done) {

                Image(
                    painter = painterResource(
                        id = R.drawable.ic_check
                    ),
                    contentDescription = "Concluída",
                    modifier = Modifier.size(18.dp),
                    colorFilter =
                        ColorFilter.tint(Color.Black)
                )
            }
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = task.text,
                color = textColor,
                fontFamily = Quicksand,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            priorityColor
                        )
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text =
                        "${task.cat} • ${priorityName(task.priority)}",
                    color = muted,
                    fontFamily = Quicksand,
                    fontSize = 9.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.width(5.dp)
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {

            TaskAction(
                icon = R.drawable.ic_edit,
                description = "Editar",
                dark = dark,
                border = border,
                onClick = onEdit
            )

            TaskAction(
                icon = R.drawable.ic_delete,
                description = "Excluir",
                dark = dark,
                border = border,
                onClick = onDelete
            )
        }
    }
}


/* =========================================================
   BOTÕES DA TAREFA
========================================================= */

@Composable
private fun TaskAction(
    icon: Int,
    description: String,
    dark: Boolean,
    border: Color,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(
                RoundedCornerShape(10.dp)
            )
            .background(
                if (dark)
                    Color(0x08FFFFFF)
                else
                    Color(0x08000000)
            )
            .border(
                1.dp,
                border,
                RoundedCornerShape(10.dp)
            )
            .clickable {
                onClick()
            },
        contentAlignment =
            Alignment.Center
    ) {

        Image(
            painter = painterResource(
                id = icon
            ),
            contentDescription = description,
            modifier = Modifier.size(16.dp),
            colorFilter =
                ColorFilter.tint(
                    if (description == "Excluir") {
                        Danger.copy(
                            alpha = .8f
                        )
                    } else if (dark) {
                        Color.White.copy(.65f)
                    } else {
                        Color.Black.copy(.60f)
                    }
                )
        )
    }
}


/* =========================================================
   ESTADO VAZIO
========================================================= */

@Composable
private fun EmptyState(
    hasTasks: Boolean,
    textColor: Color,
    muted: Color
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(
                    RoundedCornerShape(20.dp)
                )
                .background(
                    AccentSoft
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.ic_check
                ),
                contentDescription = null,
                modifier = Modifier.size(25.dp),
                colorFilter =
                    ColorFilter.tint(Accent)
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text =
                if (hasTasks)
                    "Nada encontrado"
                else
                    "Tudo tranquilo por aqui",
            color = textColor.copy(.75f),
            fontFamily = Quicksand,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text =
                if (hasTasks)
                    "Tente mudar o filtro ou a pesquisa."
                else
                    "Adicione sua primeira tarefa usando o botão +.",
            color = muted,
            fontFamily = Quicksand,
            fontSize = 10.sp
        )
    }
}


/* =========================================================
   POMODORO
========================================================= */

@Composable
private fun Pomodoro(
    time: Int,
    running: Boolean,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onAddMinute: () -> Unit,
    dark: Boolean,
    card: Color,
    border: Color,
    muted: Color
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 18.dp
            )
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(card)
            .border(
                1.dp,
                border,
                RoundedCornerShape(22.dp)
            )
            .padding(18.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "Pomodoro",
            color = muted,
            fontFamily = Quicksand,
            fontSize = 12.sp
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        val total = 25 * 60

        val progress =
            1f -
                (time.toFloat() / total.toFloat())

        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {

            CircularProgress(
                progress = progress
            )

            val minutes =
                time / 60

            val seconds =
                time % 60

            Text(
                text =
                    "$minutes:${seconds.toString().padStart(2, '0')}",
                color = Accent,
                fontFamily = Quicksand,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {

            PomoButton(
                text =
                    if (running)
                        "Pausar"
                    else
                        if (time < total)
                            "Continuar"
                        else
                            "Iniciar",
                dark = dark,
                border = border,
                modifier =
                    Modifier.weight(1f),
                onClick = onToggle
            )

            PomoButton(
                text = "Resetar",
                dark = dark,
                border = border,
                modifier =
                    Modifier.weight(1f),
                onClick = onReset
            )

            PomoButton(
                text = "+1 min",
                dark = dark,
                border = border,
                modifier =
                    Modifier.weight(1f),
                onClick = onAddMinute
            )
        }
    }
}


/* =========================================================
   CÍRCULO DO POMODORO
========================================================= */

@Composable
private fun CircularProgress(
    progress: Float
) {

    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(170.dp)
    ) {

        val strokeWidth =
            8.dp.toPx()

        val diameter =
            size.minDimension -
                strokeWidth

        drawArc(
            color =
                Color.White.copy(
                    alpha = .07f
                ),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style =
                androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth
                )
        )

        drawArc(
            color = Accent,
            startAngle = -90f,
            sweepAngle =
                360f * progress.coerceIn(
                    0f,
                    1f
                ),
            useCenter = false,
            style =
                androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth
                )
        )
    }
}


/* =========================================================
   BOTÃO POMODORO
========================================================= */

@Composable
private fun PomoButton(
    text: String,
    dark: Boolean,
    border: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(
                RoundedCornerShape(12.dp)
            )
            .background(
                if (dark)
                    Color(0x0DFFFFFF)
                else
                    Color(0x08000000)
            )
            .border(
                1.dp,
                border,
                RoundedCornerShape(12.dp)
            )
            .clickable {
                onClick()
            },
        contentAlignment =
            Alignment.Center
    ) {

        Text(
            text = text,
            color =
                if (dark)
                    Color.White
                else
                    LightText,
            fontFamily = Quicksand,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


/* =========================================================
   DIALOG DE TAREFA
========================================================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDialog(
    task: Tarefa?,
    dark: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        String,
        String,
        String
    ) -> Unit
) {

    var text by remember(task) {
        mutableStateOf(
            task?.text ?: ""
        )
    }

    var category by remember(task) {
        mutableStateOf(
            task?.cat ?: "Estudos"
        )
    }

    var priority by remember(task) {
        mutableStateOf(
            task?.priority ?: "low"
        )
    }

    var error by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        containerColor =
            if (dark)
                Color(0xFF0F0F12)
            else
                Color.White,

        title = {

            Text(
                text =
                    if (task == null)
                        "Nova tarefa"
                    else
                        "Editar tarefa",
                fontFamily = Quicksand,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color =
                    if (dark)
                        Color.White
                    else
                        LightText
            )
        },

        text = {

            Column {

                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        error = false
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "O que você precisa fazer?",
                            fontFamily = Quicksand,
                            fontSize = 13.sp
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = Quicksand,
                        fontSize = 13.sp
                    ),
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction =
                                ImeAction.Done
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {

                                if (text.trim().isNotEmpty()) {

                                    onSave(
                                        text.trim(),
                                        category,
                                        priority
                                    )
                                } else {
                                    error = true
                                }
                            }
                        ),
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor =
                                Color.Transparent,
                            unfocusedContainerColor =
                                Color.Transparent,
                            focusedIndicatorColor =
                                Accent,
                            unfocusedIndicatorColor =
                                if (dark)
                                    DarkBorder
                                else
                                    LightBorder,
                            cursorColor =
                                Accent
                        )
                )

                if (error) {

                    Text(
                        text = "Digite uma tarefa.",
                        color = Danger,
                        fontFamily = Quicksand,
                        fontSize = 10.sp,
                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                PlannerDropdown(
                    label = "Categoria",
                    value = category,
                    options =
                        listOf(
                            "Estudos",
                            "Trabalho",
                            "Pessoal",
                            "Saúde"
                        ),
                    dark = dark,
                    onChange = {
                        category = it
                    }
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                PlannerDropdown(
                    label = "Prioridade",
                    value =
                        priorityName(priority),
                    options =
                        listOf(
                            "Baixa",
                            "Média",
                            "Alta"
                        ),
                    dark = dark,
                    onChange = {

                        priority =
                            when (it) {
                                "Alta" ->
                                    "high"

                                "Média" ->
                                    "medium"

                                else ->
                                    "low"
                            }
                    }
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val clean =
                        text.trim()

                    if (clean.isEmpty()) {

                        error = true

                    } else {

                        onSave(
                            clean,
                            category,
                            priority
                        )
                    }
                }
            ) {

                Text(
                    "Salvar",
                    color = Accent,
                    fontFamily = Quicksand,
                    fontWeight = FontWeight.Bold
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    "Cancelar",
                    color =
                        if (dark)
                            Color.White.copy(.75f)
                        else
                            LightText.copy(.70f),
                    fontFamily = Quicksand
                )
            }
        }
    )
}


/* =========================================================
   DROPDOWN DO FORMULÁRIO
========================================================= */

@Composable
private fun PlannerDropdown(
    label: String,
    value: String,
    options: List<String>,
    dark: Boolean,
    onChange: (String) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Column {

        Text(
            text = label,
            color =
                if (dark)
                    Color.White.copy(.45f)
                else
                    LightText.copy(.55f),
            fontFamily = Quicksand,
            fontSize = 10.sp
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Box {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        if (dark)
                            Color.Black.copy(.35f)
                        else
                            Color(0xFFF4F4F6)
                    )
                    .border(
                        1.dp,
                        if (expanded)
                            Accent.copy(.35f)
                        else if (dark)
                            DarkBorder
                        else
                            LightBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(
                        horizontal = 13.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text = value,
                    color =
                        if (dark)
                            Color.White
                        else
                            LightText,
                    fontFamily = Quicksand,
                    fontSize = 13.sp
                )

                Image(
                    painter = painterResource(
                        id = R.drawable.ic_arrow_down
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(17.dp)
                        .rotate(
                            if (expanded)
                                180f
                            else
                                0f
                        ),
                    colorFilter =
                        ColorFilter.tint(
                            if (expanded)
                                Accent
                            else if (dark)
                                Color.White.copy(.55f)
                            else
                                Color.Black.copy(.55f)
                        )
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {

                options.forEach { option ->

                    DropdownMenuItem(
                        text = {

                            Text(
                                text = option,
                                fontFamily = Quicksand,
                                fontSize = 13.sp,
                                color =
                                    if (
                                        option == value
                                    ) {
                                        Accent
                                    } else {
                                        if (dark)
                                            Color.White
                                        else
                                            LightText
                                    }
                            )
                        },
                        onClick = {

                            onChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


/* =========================================================
   TOAST
========================================================= */

@Composable
private fun ToastMessage(
    message: String,
    dark: Boolean,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (dark)
                    Color(0xEE0F0F12)
                else
                    Color(0xEEFDFDFD)
            )
            .border(
                1.dp,
                if (dark)
                    DarkBorder
                else
                    LightBorder,
                CircleShape
            )
            .padding(
                horizontal = 15.dp,
                vertical = 10.dp
            )
    ) {

        Text(
            text = message,
            color =
                if (dark)
                    Color.White
                else
                    LightText,
            fontFamily = Quicksand,
            fontSize = 11.sp
        )
    }
}


/* =========================================================
   FILTRAR / ORDENAR
========================================================= */

private fun getVisibleTasks(
    tasks: List<Tarefa>,
    filter: String,
    search: String,
    sort: String
): List<Tarefa> {

    var result =
        tasks.filter { task ->

            val matchesFilter =
                filter == "Todos" ||
                    task.cat == filter

            val normalizedSearch =
                search
                    .lowercase(
                        java.util.Locale(
                            "pt",
                            "BR"
                        )
                    )
                    .trim()

            val matchesSearch =
                normalizedSearch.isEmpty() ||
                    task.text
                        .lowercase(
                            java.util.Locale(
                                "pt",
                                "BR"
                            )
                        )
                        .contains(
                            normalizedSearch
                        )

            matchesFilter &&
                matchesSearch
        }

    result =
        when (sort) {

            "name" -> {

                result.sortedWith(
                    compareBy(
                        String.CASE_INSENSITIVE_ORDER
                    ) {
                        it.text
                    }
                )
            }

            "priority" -> {

                val weight =
                    mapOf(
                        "high" to 3,
                        "medium" to 2,
                        "low" to 1
                    )

                result.sortedByDescending {
                    weight[it.priority] ?: 1
                }
            }

            "status" -> {

                result.sortedBy {
                    it.done
                }
            }

            else -> result
        }

    return result
}


/* =========================================================
   NOME DA PRIORIDADE
========================================================= */

private fun priorityName(
    priority: String
): String {

    return when (priority) {

        "high" ->
            "Alta"

        "medium" ->
            "Média"

        else ->
            "Baixa"
    }
}


/* =========================================================
   CRIPTO — CARREGAR
========================================================= */

private fun loadTasks(
    context: Context
): List<Tarefa> {

    return try {

        val data =
            Cripto(context)
                .carregar("tasks")

        if (data.isNullOrBlank()) {
            return emptyList()
        }

        val array =
            JSONArray(data)

        buildList {

            for (i in 0 until array.length()) {

                val item =
                    array.optJSONObject(i)
                        ?: continue

                val text =
                    item.optString(
                        "text"
                    ).trim()

                if (text.isEmpty()) {
                    continue
                }

                add(
                    Tarefa(
                        text = text,

                        cat =
                            item.optString(
                                "cat",
                                "Pessoal"
                            ),

                        priority =
                            item.optString(
                                "priority",
                                "low"
                            ),

                        done =
                            item.optBoolean(
                                "done",
                                false
                            ),

                        created =
                            item.optLong(
                                "created",
                                System.currentTimeMillis()
                            ),

                        updated =
                            item.optLong(
                                "updated",
                                System.currentTimeMillis()
                            )
                    )
                )
            }
        }

    } catch (
        error: Exception
    ) {

        error.printStackTrace()

        emptyList()
    }
}


/* =========================================================
   CRIPTO — SALVAR
========================================================= */

private fun saveTasks(
    context: Context,
    tasks: List<Tarefa>
) {

    try {

        val array =
            JSONArray()

        tasks.forEach { task ->

            val objectJson =
                JSONObject()

            objectJson.put(
                "text",
                task.text
            )

            objectJson.put(
                "cat",
                task.cat
            )

            objectJson.put(
                "priority",
                task.priority
            )

            objectJson.put(
                "done",
                task.done
            )

            objectJson.put(
                "created",
                task.created
            )

            objectJson.put(
                "updated",
                task.updated
            )

            array.put(
                objectJson
            )
        }

        Cripto(context)
            .salvar(
                "tasks",
                array.toString()
            )

    } catch (
        error: Exception
    ) {

        error.printStackTrace()
    }
}