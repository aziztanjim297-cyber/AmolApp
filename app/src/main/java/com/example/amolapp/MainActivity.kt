package com.example.amolapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Green = Color(0xFF07543F)
data class Amal(val id:String,val name:String,val detail:String,val group:String,val fard:Boolean=false)
private val amals = listOf(
 Amal("tahajjud","তাহাজ্জুদ","ফজরের ৩০ মিনিট আগে; কমপক্ষে ৪ রাকাত, দোয়া ও ইস্তিগফার","ফজরের আগে"),
 Amal("fajr_s","ফজরের সুন্নত","২ রাকাত","ফজর",true), Amal("fajr_f","ফজরের ফরজ","জামাতে/সময়মতো","ফজর",true),
 Amal("yasin","সূরা ইয়াসিন","ফজরের পর","ফজরের পর"), Amal("morning","সকালবেলার মাসনুন দোয়া ও জিকির","ফজরের পর","ফজরের পর"),
 Amal("quran","কুরআন তিলাওয়াত","সকালে অর্থসহ ২ পৃষ্ঠা","ফজরের পর"), Amal("ishraq","ইশরাক","৪ রাকাত","ইশরাক"),
 Amal("zuhr_s1","যোহরের আগের সুন্নত","৪ রাকাত","যোহর"), Amal("zuhr_f","যোহরের ফরজ","সময়মতো","যোহর",true),
 Amal("zuhr_s2","যোহরের পরের সুন্নত","২ রাকাত","যোহর"), Amal("asr_f","আসরের ফরজ","সময়মতো","আসর",true),
 Amal("asr_s","আসরের পরের আমল","ব্যক্তিগত অনুসরণ অনুযায়ী","আসর"), Amal("evening","বিকেলের জিকির ও দোয়া","আসর-পরবর্তী সময়ে","বিকেল"),
 Amal("mag_f","মাগরিবের ফরজ","সময়মতো","মাগরিব",true), Amal("mag_s","মাগরিবের সুন্নত","২ রাকাত","মাগরিব",true),
 Amal("awwabin","আওয়াবিন","নফল নামাজ","মাগরিব"), Amal("waqiah","সূরা ওয়াকিয়াহ","মাগরিবের পর","মাগরিবের পর"),
 Amal("isha_f","ইশার ফরজ","সময়মতো","ইশা",true), Amal("isha_s","ইশার সুন্নত","সুন্নত নামাজ","ইশা"),
 Amal("witr","বিতর","৩ রাকাত","ইশা"), Amal("mulk","সূরা মুলক","রাতে","ঘুমের আগে"),
 Amal("sleep","ঘুমের আগের আমল","ওজু, আয়াতুল কুরসি, ইখলাস, ফালাক, নাস ও তাসবিহে ফাতেমি","ঘুমের আগে"),
 Amal("istighfar","ইস্তিগফার ও দরুদ","দৈনিক নির্ধারিত পরিমাণ","সারাদিন"), Amal("muhasaba","দৈনিক মুহাসাবা","দিনের কাজ পর্যালোচনা ও তাওবা","ঘুমের আগে")
)
class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{App()}} }
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun App(){
 val ctx= LocalContext.current
 val pref= remember{ctx.getSharedPreferences("amol_v2",0)}
 val day= remember{SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())}
 val date= remember{SimpleDateFormat("EEEE, dd MMMM yyyy",Locale("bn","BD")).format(Date())}
 var done by remember{mutableStateOf(pref.getStringSet("done_$day",emptySet())?.toSet()?:emptySet())}
 var page by remember{mutableStateOf(0)}
 var dark by remember{mutableStateOf(pref.getBoolean("dark",false))}
 var count by remember{mutableIntStateOf(pref.getInt("zikr",0))}
 MaterialTheme(colorScheme=if(dark) darkColorScheme(primary=Color(0xFF70D5A5)) else lightColorScheme(primary=Green)){
 Scaffold(topBar={CenterAlignedTopAppBar(title={Text("দৈনন্দিন আমল",fontWeight=FontWeight.Bold)},actions={IconButton(onClick={dark=!dark;pref.edit().putBoolean("dark",dark).apply()}){Icon(Icons.Default.Brightness6,null)}})},
 bottomBar={NavigationBar{
 NavigationBarItem(page==0,{page=0},{Icon(Icons.Default.Home,null)},label={Text("হোম")})
 NavigationBarItem(page==1,{page=1},{Icon(Icons.Default.CheckCircle,null)},label={Text("আমল")})
 NavigationBarItem(page==2,{page=2},{Icon(Icons.Default.Refresh,null)},label={Text("জিকির")})
 NavigationBarItem(page==3,{page=3},{Icon(Icons.Default.Settings,null)},label={Text("সেটিংস")})
 }}){pad-> when(page){
 0->Column(Modifier.padding(pad).fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
 Card(colors=CardDefaults.cardColors(containerColor=Green)){Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
 Text(date,color=Color.White);Text("আল্লাহর সন্তুষ্টির পথে",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.Bold)
 Text("আজকের আমল: ${done.size} / ${amals.size}",color=Color.White)
 LinearProgressIndicator(progress={done.size.toFloat()/amals.size},modifier=Modifier.fillMaxWidth())
 }}
 Button(onClick={page=1},Modifier.fillMaxWidth()){Text("আজকের আমল দেখুন")}
 OutlinedButton(onClick={page=2},Modifier.fillMaxWidth()){Text("জিকির কাউন্টার")}
 Text("নামাজের সময় ও অ্যালার্ম পরবর্তী ধাপে যুক্ত হবে।")
 }
 1->LazyColumn(Modifier.padding(pad).fillMaxSize().padding(horizontal=12.dp)){
 item{Text("আজকের আমল",Modifier.padding(12.dp),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
 items(amals){a->
 var groupLabel=a.group
 Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){
 Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){
 val checked=a.id in done
 Checkbox(checked=checked,onCheckedChange={v->if(v&&!checked){done=done+a.id;pref.edit().putStringSet("done_$day",done).apply()}},enabled=!checked)
 Column(Modifier.weight(1f)){
 Text(a.name,fontWeight=FontWeight.SemiBold)
 Text(a.detail,style=MaterialTheme.typography.bodySmall)
 if(a.fard)Text("ফরজ/আবশ্যক",color=MaterialTheme.colorScheme.primary,fontSize=10.sp)
 if(checked)Text("✓ সম্পন্ন — পরিবর্তন করা যাবে না",color=Color(0xFF16834A),fontSize=11.sp)
 }
 if(checked)Icon(Icons.Default.Lock,null,tint=Color(0xFF16834A))
 }}
 }
 }
 2->Column(Modifier.padding(pad).fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
 Text("জিকির কাউন্টার",style=MaterialTheme.typography.headlineSmall);Text("$count",fontSize=64.sp,color=MaterialTheme.colorScheme.primary)
 Button(onClick={count++;pref.edit().putInt("zikr",count).apply()},Modifier.fillMaxWidth()){Text("+১")}
 TextButton(onClick={count=0;pref.edit().putInt("zikr",0).apply()}){Text("রিসেট")}
 }
 else->Column(Modifier.padding(pad).padding(20.dp)){
 Text("সেটিংস",style=MaterialTheme.typography.headlineSmall);Row(verticalAlignment=Alignment.CenterVertically){Text("ডার্ক থিম",Modifier.weight(1f));Switch(dark,{dark=it;pref.edit().putBoolean("dark",it).apply()})}
 Text("অ্যাপ: দৈনন্দিন আমল");Text("পরবর্তী ধাপ: হানাফি নামাজের সময়, সময়ভিত্তিক আমল, অ্যালার্ম, ভাষা, কুরআন ও হাদিস।")
 }
 }}}
}
