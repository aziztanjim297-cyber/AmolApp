@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.amolapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

private val Green = Color(0xFF00796B)
private val LightGreen = Color(0xFFE8F5F2)

 data class Amal(val id: Long, val title: String, val description: String, val category: String, val target: String = "")

data class Counter(val name: String, val value: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AmalApp() } }
}

@Composable
fun AmalApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { Store(context) }
    var tab by remember { mutableIntStateOf(0) }
    var amals by remember { mutableStateOf(store.loadAmals()) }
    var checked by remember { mutableStateOf(store.loadChecked(todayKey())) }
    var counters by remember { mutableStateOf(store.loadCounters()) }
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Amal?>(null) }

    MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = Color(0xFF2E7D32))) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(listOf("আমার আমল", "দৈনিক আমল", "জিকির কাউন্টার", "মুহাসাবা", "সেটিংস")[tab], fontWeight = FontWeight.Bold) }) },
            bottomBar = {
                NavigationBar {
                    listOf(Icons.Default.Home to "হোম", Icons.Default.CheckCircle to "আমল", Icons.Default.Repeat to "কাউন্টার", Icons.Default.EditNote to "মুহাসাবা", Icons.Default.Settings to "সেটিংস").forEachIndexed { i, pair ->
                        NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(pair.first, null) }, label = { Text(pair.second, fontSize = 11.sp) })
                    }
                }
            }, floatingActionButton = {
                if (tab == 1 || tab == 4) FloatingActionButton(onClick = { editing = null; showEditor = true }) { Icon(Icons.Default.Add, "নতুন") }
            }
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad)) {
                when(tab) {
                    0 -> HomeScreen(amalList = amals, checked = checked, onCheck = { id, v -> checked = checked.toMutableSet().apply { if(v) add(id) else remove(id) }; store.saveChecked(todayKey(), checked) }, onOpenAmal = { tab = 1 })
                    1 -> DailyScreen(amalList = amals, checked = checked, onCheck = { id, v -> checked = checked.toMutableSet().apply { if(v) add(id) else remove(id) }; store.saveChecked(todayKey(), checked) }, onEdit = { editing = it; showEditor = true }, onDelete = { a -> amals = amals.filterNot { it.id == a.id }; store.saveAmals(amals) })
                    2 -> CounterScreen(counters = counters, onChange = { name, value -> counters = counters.map { if(it.name == name) it.copy(value = value.coerceAtLeast(0)) else it }; store.saveCounters(counters) }, onAdd = { name -> counters = counters + Counter(name,0); store.saveCounters(counters) })
                    3 -> MuhasabahScreen(store)
                    4 -> SettingsScreen(amalList = amals, onEdit = { editing = it; showEditor = true }, onDelete = { a -> amals = amals.filterNot { it.id == a.id }; store.saveAmals(amals) }, onReset = { amals = Store.defaultAmals(); store.saveAmals(amals) })
                }
            }
        }
    }
    if(showEditor) {
        AmalEditor(initial = editing, onDismiss = { showEditor = false }, onSave = { a ->
            amals = if(editing == null) amals + a else amals.map { if(it.id == a.id) a else it }
            store.saveAmals(amals); showEditor = false
        })
    }
}

@Composable fun HomeScreen(amalList: List<Amal>, checked: Set<Long>, onCheck:(Long,Boolean)->Unit, onOpenAmal:()->Unit) {
    val percent = if(amalList.isEmpty()) 0 else checked.intersect(amalList.map { it.id }.toSet()).size * 100 / amalList.size
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text(dateText(), color = Color.Gray); Spacer(Modifier.height(4.dp)); Card(colors = CardDefaults.cardColors(containerColor = LightGreen)) { Column(Modifier.padding(18.dp)) { Text("আজকের আমল", fontSize=20.sp, fontWeight=FontWeight.Bold); Text("$percent% সম্পন্ন", fontSize=28.sp, color=Green, fontWeight=FontWeight.Bold); LinearProgressIndicator({ percent/100f }, Modifier.fillMaxWidth().padding(top=10.dp)) } } }
        item { Text("আজকের আমলসমূহ", fontWeight=FontWeight.Bold, fontSize=19.sp) }
        items(amalList.take(8)) { a -> AmalRow(a, checked.contains(a.id), { onCheck(a.id, !checked.contains(a.id)) }, null) }
        item { Button(onClick=onOpenAmal, modifier=Modifier.fillMaxWidth()) { Text("সব আমল দেখুন") } }
    }
}

@Composable fun DailyScreen(amalList: List<Amal>, checked:Set<Long>, onCheck:(Long,Boolean)->Unit, onEdit:(Amal)->Unit, onDelete:(Amal)->Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
        item { Text(dateText(), fontWeight=FontWeight.Bold, fontSize=18.sp); Text("ট্যাপ করে আজকের আমল সম্পন্ন করুন", color=Color.Gray) }
        items(amalList) { a -> AmalRow(a, checked.contains(a.id), { onCheck(a.id, !checked.contains(a.id)) }, { onEdit(a) }, { onDelete(a) }) }
    }
}

@Composable fun AmalRow(a: Amal, done:Boolean, toggle:()->Unit, edit:(()->Unit)?, delete:(()->Unit)? = null) {
    Card(shape=RoundedCornerShape(14.dp)) { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment=Alignment.CenterVertically) {
        Checkbox(checked=done, onCheckedChange={ toggle() }); Column(Modifier.weight(1f)) { Text(a.title, fontWeight=FontWeight.SemiBold); if(a.description.isNotBlank()) Text(a.description, fontSize=12.sp, color=Color.Gray, maxLines=2); if(a.target.isNotBlank()) Text("লক্ষ্য: ${a.target}", fontSize=12.sp, color=Green) }
        edit?.let { IconButton(onClick=it) { Icon(Icons.Default.Edit, "এডিট") } }; delete?.let { IconButton(onClick=it) { Icon(Icons.Default.Delete, "ডিলিট") } }
    } }
}

@Composable fun CounterScreen(counters:List<Counter>, onChange:(String,Int)->Unit, onAdd:(String)->Unit) {
    var show by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text("জিকির ও দোয়া কাউন্টার", fontSize=20.sp, fontWeight=FontWeight.Bold); Text("দরুদ, ইস্তিগফার বা অন্য যিকিরের সংখ্যা রাখুন", color=Color.Gray) }
        items(counters) { c -> Card { Column(Modifier.padding(16.dp)) { Text(c.name, fontWeight=FontWeight.Bold, fontSize=18.sp); Text("${c.value} বার", fontSize=26.sp, color=Green, modifier=Modifier.fillMaxWidth(), textAlign=TextAlign.Center); Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly) { OutlinedButton({onChange(c.name,c.value-10)}){Text("-10")}; OutlinedButton({onChange(c.name,c.value-1)}){Text("-1")}; Button({onChange(c.name,0)}){Text("0")}; Button({onChange(c.name,c.value+1)}){Text("+1")}; Button({onChange(c.name,c.value+10)}){Text("+10")} } } } }
        item { Button(onClick={show=true}, modifier=Modifier.fillMaxWidth()){Text("+ নতুন কাউন্টার") } }
    }
    if(show) NameDialog("নতুন কাউন্টার", { onAdd(it); show=false }, {show=false})
}

@Composable fun MuhasabahScreen(store:Store) {
    var rating by remember { mutableIntStateOf(store.getInt("rating",0)) }
    var mistakes by remember { mutableStateOf(store.getString("mistakes","")) }
    var missed by remember { mutableStateOf(store.getString("missed","")) }
    var plan by remember { mutableStateOf(store.getString("plan","")) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item { Text(dateText(), color=Color.Gray); Text("আজকের মুহাসাবা", fontSize=22.sp, fontWeight=FontWeight.Bold) }
        item { Text("আজকের আমল কেমন হয়েছে?", fontWeight=FontWeight.Bold); Row { (1..5).forEach { n -> TextButton({ rating=n; store.putInt("rating",n) }) { Text(if(n<=rating) "★" else "☆", fontSize=30.sp, color=if(n<=rating) Green else Color.Gray) } } } }
        item { Field("আজকের ভুলত্রুটি", mistakes){mistakes=it; store.putString("mistakes",it)} }
        item { Field("কোন আমল বাদ গেছে?", missed){missed=it; store.putString("missed",it)} }
        item { Field("আগামীকাল কীভাবে আরও ভালো করব?", plan){plan=it; store.putString("plan",it)} }
        item { Button(onClick={store.putString("savedAt",todayKey())}, modifier=Modifier.fillMaxWidth()){Text("মুহাসাবা সংরক্ষণ করুন") } }
    }
}

@Composable fun Field(label:String, value:String, onChange:(String)->Unit) { OutlinedTextField(value=value,onValueChange=onChange,label={Text(label)},modifier=Modifier.fillMaxWidth(),minLines=3) }

@Composable fun SettingsScreen(amalList:List<Amal>, onEdit:(Amal)->Unit, onDelete:(Amal)->Unit, onReset:()->Unit) {
    var confirm by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
        item { Text("আমল ব্যবস্থাপনা", fontSize=21.sp, fontWeight=FontWeight.Bold); Text("নাম, বিবরণ ও লক্ষ্য পরিবর্তন বা নতুন আমল যোগ করা যাবে।", color=Color.Gray) }
        items(amalList) { a -> AmalRow(a,true,{},{onEdit(a)},{onDelete(a)}) }
        item { OutlinedButton(onClick={confirm=true},modifier=Modifier.fillMaxWidth()){Text("ডিফল্ট আমল পুনরুদ্ধার") } }
    }
    if(confirm) AlertDialog(onDismissRequest={confirm=false}, title={Text("নিশ্চিত করবেন?")}, text={Text("আপনার কাস্টম আমল তালিকা ডিফল্ট তালিকায় ফিরে যাবে।")}, confirmButton={TextButton({onReset();confirm=false}){Text("হ্যাঁ")}}, dismissButton={TextButton({confirm=false}){Text("না")}})
}

@Composable fun AmalEditor(initial:Amal?, onDismiss:()->Unit, onSave:(Amal)->Unit) {
    var title by remember { mutableStateOf(initial?.title ?: "") }; var desc by remember { mutableStateOf(initial?.description ?: "") }; var cat by remember { mutableStateOf(initial?.category ?: "অন্যান্য") }; var target by remember { mutableStateOf(initial?.target ?: "") }
    AlertDialog(onDismissRequest=onDismiss, title={Text(if(initial==null) "নতুন আমল যোগ করুন" else "আমল এডিট করুন")}, text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(title,{title=it},label={Text("আমলের নাম")});OutlinedTextField(desc,{desc=it},label={Text("বিবরণ")},minLines=3);OutlinedTextField(cat,{cat=it},label={Text("ক্যাটাগরি")});OutlinedTextField(target,{target=it},label={Text("লক্ষ্য/পরিমাণ")})}}, confirmButton={Button(enabled=title.isNotBlank(),onClick={onSave(Amal(initial?.id ?: System.currentTimeMillis(),title,desc,cat,target))}){Text("সংরক্ষণ")}}, dismissButton={TextButton(onClick=onDismiss){Text("বাতিল")}})
}

@Composable fun NameDialog(title:String, onOk:(String)->Unit, onCancel:()->Unit) { var name by remember {mutableStateOf("")}; AlertDialog(onDismissRequest=onCancel,title={Text(title)},text={OutlinedTextField(name,{name=it},label={Text("নাম")})},confirmButton={Button(enabled=name.isNotBlank(),onClick={onOk(name)}){Text("যোগ করুন")}},dismissButton={TextButton(onClick=onCancel){Text("বাতিল")}}) }

private fun todayKey() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
private fun dateText() = SimpleDateFormat("dd MMMM yyyy", Locale("bn","BD")).format(Date())

class Store(private val context:Context) {
    private val p=context.getSharedPreferences("amol_store",Context.MODE_PRIVATE)
    fun loadAmals():List<Amal>{ val raw=p.getString("amals",null) ?: return defaultAmals(); return runCatching { val a=JSONArray(raw); List(a.length()){i-> val o=a.getJSONObject(i); Amal(o.getLong("id"),o.getString("title"),o.getString("description"),o.getString("category"),o.optString("target"))} }.getOrElse{defaultAmals()} }
    fun saveAmals(list:List<Amal>){ val a=JSONArray(); list.forEach{a.put(JSONObject().apply{put("id",it.id);put("title",it.title);put("description",it.description);put("category",it.category);put("target",it.target)})}; p.edit().putString("amals",a.toString()).apply() }
    fun loadChecked(day:String):Set<Long>{ return p.getStringSet("checked_$day",emptySet())!!.mapNotNull{it.toLongOrNull()}.toSet() }
    fun saveChecked(day:String,set:Set<Long>){p.edit().putStringSet("checked_$day",set.map{it.toString()}.toSet()).apply()}
    fun loadCounters():List<Counter>{ val raw=p.getString("counters",null); if(raw==null){val d=listOf(Counter("দরুদ শরীফ",0),Counter("আস্তাগফিরুল্লাহ",0));saveCounters(d);return d}; return runCatching{val a=JSONArray(raw);List(a.length()){i->val o=a.getJSONObject(i);Counter(o.getString("name"),o.getInt("value"))}}.getOrDefault(emptyList()) }
    fun saveCounters(list:List<Counter>){val a=JSONArray();list.forEach{a.put(JSONObject().apply{put("name",it.name);put("value",it.value)})};p.edit().putString("counters",a.toString()).apply()}
    fun getInt(k:String,d:Int)=p.getInt(k,d); fun putInt(k:String,v:Int)=p.edit().putInt(k,v).apply(); fun getString(k:String,d:String)=p.getString(k,d)?:d; fun putString(k:String,v:String)=p.edit().putString(k,v).apply()
    companion object { fun defaultAmals()=listOf(
        Amal(1,"তাহাজ্জুদ সালাত","ফজরের ওয়াক্ত হওয়ার ৩০ মিনিট আগে ঘুম থেকে উঠে অন্তত ৪ রাকাত সালাতুত তাহাজ্জুদ আদায় করা এবং দোয়া ইস্তিগফার করা।","রাত","৪ রাকাত"),
        Amal(2,"ফজরের সালাত","সুন্নাত পড়ে সঠিক সময়ে জামাআতের সাথে (পুরুষদের ক্ষেত্রে) ফজরের সালাত আদায় করা।","সকাল"),
        Amal(3,"সকালের জিকির ও দোয়া","ফজরের পর কিছুক্ষণ বসে সুরা ইয়াসিন, মাসনুন দোয়া ও সকালের জিকিরগুলো পড়া।","সকাল"),
        Amal(4,"কুরআন তিলাওয়াত","প্রতিদিন সকালে ২ পৃষ্ঠা অর্থসহ কুরআন তিলাওয়াত করা।","সকাল","২ পৃষ্ঠা"),
        Amal(5,"সালাতুল ইশরাক","সূর্য ওঠার পর ৪ রাকাত ইশরাকের সালাত আদায় করা।","সকাল","৪ রাকাত"),
        Amal(6,"যোহরের সালাত","সঠিক সময়ে আগে পরের সুন্নাতসহ যোহরের সালাত আদায় করা।","দিন"),
        Amal(7,"আসরের সালাত","আসরের সালাত ২ রাকাত সুন্নতসহ সময়মতো আদায় করা।","দিন"),
        Amal(8,"বিকেলের জিকির ও দোয়া","আসরের পর সন্ধ্যা হওয়ার পূর্ব মুহূর্তে বিকেলের মাসনুন জিকির ও দোয়া করা।","দিন"),
        Amal(9,"মাগরিবের সালাত","মাগরিবের ফরজ ও সুন্নাত আদায় করা এবং আউয়াবীন সালাত পড়া। এরপর সুরা ওয়াকিয়া তেলাওয়াত করা।","সন্ধ্যা"),
        Amal(10,"এশার সালাত ও বিতর","আগে পরের সুন্নতসহ এশার সালাত এবং এর পর বিতরের সালাত আদায় করা। এরপর সুরা মূলক তেলাওয়াত করা।","রাত"),
        Amal(11,"ওজু করা","ঘুমানোর পূর্বে ভালোভাবে ওজু করে নেওয়া।","রাত"),
        Amal(12,"ঘুমানোর মাসনুন দোয়া ও জিকির","আয়াতুল কুরসি, সূরা আল-ইখলাস, সূরা আল-ফালাক, সূরা আন-নাস ও সূরা আল-মুলক তিলাওয়াত করা।","রাত"),
        Amal(13,"তাসবিহ পাঠ","তাসবিহে ফাতেমি: ৩৩ বার সুবহানাল্লাহ, ৩৩ বার আলহামদুলিল্লাহ, ৩৪ বার আল্লাহু আকবার।","রাত","১০০ বার"),
        Amal(14,"তওবা ও আত্মসমালোচনা","সারাদিনের ভুলত্রুটির জন্য তওবা-ইস্তিগফার করা এবং অন্য সবার প্রতি মন পরিষ্কার করে ঘুমাতে যাওয়া।","রাত"),
        Amal(15,"দরুদ শরীফ পাঠ","সারাদিনে কাজের ফাঁকে ফাঁকে রাসুলুল্লাহ (সা.)-এর ওপর কতবার দরুদ পাঠ করা হয়েছে।","সারাদিন"),
        Amal(16,"আস্তাগফিরুল্লাহ পাঠ","সারাদিনে কতবার আস্তাগফিরুল্লাহ পড়া হয়েছে তা গণনা করুন।","সারাদিন"),
        Amal(17,"রাতের আমলের মুহাসাবা","রাতের বেলা সর্বশেষ নিজের আমলের মুহাসাবা করা।","রাত")
    ) }
}
