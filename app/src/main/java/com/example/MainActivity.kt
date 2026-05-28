package com.example

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Contact
import com.example.data.GeneratedWebsite
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.UserStatus
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NoCodeWebBuilderApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoCodeWebBuilderApp(viewModel: MainViewModel = viewModel()) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val userStatus by viewModel.userStatus.collectAsState()
    val currentPhone by viewModel.currentUserPhone.collectAsState()
    val profileName by viewModel.currentProfileName.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showPaywallDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Website Creator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoSecondary
                            )
                            Text(
                                text = "No-Code Instant Draft",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoOnSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                actions = {
                    // Modern bento avatar-style badge
                    Row(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(BentoLavender)
                            .clickable { showProfileDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("profile_badge_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BentoSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = profileName.split(" ")
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .joinToString("")
                                .take(2)
                            Text(
                                text = if (initials.isNotEmpty()) initials else "MW",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = profileName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 60.dp)
                            )
                            Text(
                                text = when (userStatus) {
                                    is UserStatus.ExemptFree -> "VIP (Free)"
                                    is UserStatus.SubscribedPaid -> "Paid Plan"
                                    is UserStatus.UnpaidRestricted -> "Unpaid"
                                },
                                fontSize = 8.sp,
                                color = when (userStatus) {
                                    is UserStatus.ExemptFree -> Color(0xFF00796B)
                                    is UserStatus.SubscribedPaid -> Color(0xFF00796B)
                                    is UserStatus.UnpaidRestricted -> Color(0xFFDC2626)
                                },
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoBg,
                    titleContentColor = BentoOnSurface
                )
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = BentoBorder, thickness = 1.dp)
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = activeScreen is Screen.Builder || activeScreen is Screen.ViewWebsite,
                        onClick = { viewModel.activeScreen.value = Screen.Builder },
                        icon = { Icon(Icons.Default.Build, contentDescription = "Builder Screen") },
                        label = { Text("Builder") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoPrimary,
                            selectedTextColor = BentoPrimary,
                            indicatorColor = BentoLavender,
                            unselectedIconColor = BentoOnSurface.copy(alpha = 0.5f),
                            unselectedTextColor = BentoOnSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_builder_tab")
                    )
                    NavigationBarItem(
                        selected = activeScreen is Screen.Contacts,
                        onClick = { viewModel.activeScreen.value = Screen.Contacts },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Contacts Screen") },
                        label = { Text("Inner Circle") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoPrimary,
                            selectedTextColor = BentoPrimary,
                            indicatorColor = BentoLavender,
                            unselectedIconColor = BentoOnSurface.copy(alpha = 0.5f),
                            unselectedTextColor = BentoOnSurface.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_contacts_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = activeScreen,
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    is Screen.Builder -> {
                        BuilderScreen(
                            viewModel = viewModel,
                            onPaywallRequested = { showPaywallDialog = true }
                        )
                    }
                    is Screen.Contacts -> {
                        ContactsScreen(
                            viewModel = viewModel,
                            onAddContactRequested = { showAddContactDialog = true }
                        )
                    }
                    is Screen.ViewWebsite -> {
                        ViewWebsiteScreen(
                            websiteId = screen.websiteId,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        ProfileSwitcherDialog(
            currentPhone = currentPhone,
            onDismiss = { showProfileDialog = false },
            onSave = { newPhone ->
                viewModel.updateProfilePhone(newPhone)
                showProfileDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onAdd = { name, phone, isInner ->
                viewModel.addContact(name, phone, isInner)
                showAddContactDialog = false
            }
        )
    }

    if (showPaywallDialog) {
        PaywallAndMpesaDialog(
            currentPhone = currentPhone,
            onDismiss = { showPaywallDialog = false },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BuilderScreen(
    viewModel: MainViewModel,
    onPaywallRequested: () -> Unit
) {
    val description by viewModel.inputDescription.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationError by viewModel.generationError.collectAsState()
    val websites by viewModel.allWebsites.collectAsState()
    val userStatus by viewModel.userStatus.collectAsState()
    val contacts by viewModel.allContacts.collectAsState()
    val context = LocalContext.current

    val hasAccess = userStatus !is UserStatus.UnpaidRestricted

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Build Websites Instantly",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoSecondary
                            )
                            Text(
                                text = "Instant Standalone Previews",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoOnSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Simply describe what you need (e.g., 'A vintage clothing boutique with warm palette' or 'A personal consulting portfolio page') and let Gemini formulate styled standalone HTML.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoOnSurface.copy(alpha = 0.7f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Pricing Bento Card (col-span-1, row-span-2)
                Card(
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoPink),
                    border = BorderStroke(1.dp, BentoPinkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = BentoPinkOn,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "WEEKLY PLAN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPinkOn,
                                letterSpacing = 1.sp
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "50",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BentoPinkOn
                                )
                                Text(
                                    text = " / kes",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BentoPinkOn.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Text(
                                text = "UNLIMITED SITES",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BentoPinkOn.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 2. Direct Pay Card or VIP Active Card (dark look)
                val isVip = userStatus is UserStatus.ExemptFree
                val isPaid = userStatus is UserStatus.SubscribedPaid

                Card(
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isVip) Color(0xFFE0F2F1) else BentoDarkCard
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isVip) Color(0xFF80CBC4) else Color(0xFF1D1B20)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (!hasAccess) {
                                onPaywallRequested()
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            if (isVip) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF009688)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "FREE ACCESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00796B)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BentoMpesaGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "M",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "DIRECT PAY VIA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray
                                )
                            }
                        }

                        Column {
                            if (isVip) {
                                Text(
                                    text = "Inner Circle",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF004D40)
                                )
                                Text(
                                    text = "EXEMPT ACCOUNT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00796B)
                                )
                            } else if (isPaid) {
                                Text(
                                    text = "Active Plan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "VERIFIED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoMpesaGreen
                                )
                            } else {
                                Text(
                                    text = "0715871815",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "VERIFIED MERCHANT",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoMpesaGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROJECT DESCRIPTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = BentoOnSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { viewModel.inputDescription.value = it },
                        placeholder = {
                            Text(
                                "A sleek landing page for a coffee shop containing catalog, hours, prices, and online contact forms...",
                                fontSize = 13.sp,
                                color = BentoOnSurface.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("description_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder,
                            focusedTextColor = BentoOnSurface,
                            unfocusedTextColor = BentoOnSurface
                        ),
                        enabled = hasAccess && !isGenerating,
                        maxLines = 8
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "TEMPLATE LIBRARY (10 DIVERSE SCHEMES)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    var selectedCategory by remember { mutableStateOf("All") }
                    val categories = listOf("All", "Portfolio", "Business", "Blog", "Event")
                    
                    // Horizontal scrollable category pill filtering
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                onClick = { selectedCategory = category },
                                shape = RoundedCornerShape(100.dp),
                                color = if (isSelected) BentoPrimary else BentoLavender.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, if (isSelected) BentoPrimary else BentoBorder.copy(alpha = 0.3f)),
                                modifier = Modifier.testTag("category_tab_$category")
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else BentoLavenderOn,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredTemplates = remember(selectedCategory) {
                        if (selectedCategory == "All") {
                            com.example.data.TemplateLibrary.list
                        } else {
                            com.example.data.TemplateLibrary.list.filter { it.category == selectedCategory }
                        }
                    }

                    // Gorgeous horizontal carousel of the 10 diverse templates
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                    ) {
                        items(filteredTemplates) { template ->
                            Card(
                                elevation = CardDefaults.cardElevation(0.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier
                                    .width(260.dp)
                                    .fillMaxHeight()
                                    .testTag("template_card_${template.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(template.accentColorHex).copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = template.getIcon(),
                                                    contentDescription = null,
                                                    tint = Color(template.accentColorHex),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = template.title,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = template.category.uppercase(),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(template.accentColorHex)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = template.shortDescription,
                                            fontSize = 10.sp,
                                            color = BentoOnSurface.copy(alpha = 0.6f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 13.sp
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (hasAccess && !isGenerating) {
                                                    viewModel.inputDescription.value = template.fullPrompt
                                                    Toast.makeText(context, "Filled! You can customize description now.", Toast.LENGTH_SHORT).show()
                                                } else if (!hasAccess) {
                                                    onPaywallRequested()
                                                }
                                            },
                                            shape = RoundedCornerShape(100.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = BentoLavender,
                                                contentColor = BentoLavenderOn
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(28.dp)
                                                .testTag("template_custom_${template.id}")
                                        ) {
                                            Text("Customize", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                if (hasAccess && !isGenerating) {
                                                    viewModel.inputDescription.value = template.fullPrompt
                                                    viewModel.generateNewWebsite()
                                                } else if (!hasAccess) {
                                                    onPaywallRequested()
                                                }
                                            },
                                            shape = RoundedCornerShape(100.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(template.accentColorHex),
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(28.dp)
                                                .testTag("template_draft_${template.id}")
                                        ) {
                                            Text("Instant Draft", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (generationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Generation Error: $generationError",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isGenerating) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = BentoPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Formulating Standalone HTML/CSS via Gemini...",
                                fontSize = 12.sp,
                                color = BentoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (hasAccess) {
                                    viewModel.generateNewWebsite()
                                } else {
                                    onPaywallRequested()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_website_button"),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasAccess) BentoPrimary else Color(0xFFDC2626)
                            )
                        ) {
                            Icon(
                                imageVector = if (hasAccess) Icons.Default.PlayArrow else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasAccess) "Build Website Now" else "Pay KES 50 to Unlock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Inner Circle Access",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = BentoPrimary
                        ) {
                            Text(
                                text = "FREE ACCESS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (contacts.isEmpty()) {
                        Text(
                            text = "Add contacts to grant instant VIP exemptions.",
                            fontSize = 12.sp,
                            color = BentoOnSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-8).dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            contacts.take(5).forEachIndexed { index, contact ->
                                val palette = when (index % 4) {
                                    0 -> Pair(Color(0xFFDBEAFE), Color(0xFF1E40AF))
                                    1 -> Pair(Color(0xFFD1FAE5), Color(0xFF065F46))
                                    2 -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
                                    else -> Pair(Color(0xFFF3E8FF), Color(0xFF6B21A8))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(palette.first)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = contact.name.split(" ")
                                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                        .joinToString("")
                                        .take(2)
                                    Text(
                                        text = initials.ifEmpty { "U" },
                                        fontSize = 11.sp,
                                        color = palette.second,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (contacts.size > 5) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(BentoSurface)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${contacts.size - 5}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { viewModel.activeScreen.value = Screen.Contacts },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, BentoPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Manage Inner Circle Contacts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Previously Formulated Pages (${websites.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnSurface
                )
                if (websites.isNotEmpty()) {
                    Text(
                        text = "Hold to delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoOnSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (websites.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = BentoSurface.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = BentoOnSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No Websites Formulated",
                            fontSize = 14.sp,
                            color = BentoOnSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Describe your business ideas above to build pristine previews instantly.",
                            fontSize = 12.sp,
                            color = BentoOnSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(websites, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { viewModel.activeScreen.value = Screen.ViewWebsite(item.id) },
                            onLongClick = {
                                viewModel.deleteWebsite(item.id)
                                Toast
                                    .makeText(context, "Website draft deleted.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        )
                        .testTag("website_item_${item.id}"),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                color = BentoOnSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = BentoOnSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsScreen(
    viewModel: MainViewModel,
    onAddContactRequested: () -> Unit
) {
    val contacts by viewModel.allContacts.collectAsState()
    val activePhone by viewModel.currentUserPhone.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                elevation = CardDefaults.cardElevation(0.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Inner Circle Access System",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Website Creator is completely free to contacts you choose in your Inner Circle. Anyone else will be prompted to subscribe to the standard weekly plan of KES 50.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoOnSurface.copy(alpha = 0.7f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BentoBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SIMULATED CURRENT DEVICE NUMBER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = BentoOnSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BentoBorder),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Current: $activePhone",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoOnSurface
                                )
                            }
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Tip: Add a contact below, then tap their card to simulate logging in as them!", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoLavender,
                                contentColor = BentoLavenderOn
                            ),
                            modifier = Modifier.size(46.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inner Circle Contacts (${contacts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnSurface
                )

                Button(
                    onClick = onAddContactRequested,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier
                        .testTag("add_contact_button")
                        .height(42.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Contact", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (contacts.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = BentoSurface.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BentoOnSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No Contacts Registered",
                            fontSize = 14.sp,
                            color = BentoOnSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            items(contacts, key = { it.id }) { contact ->
                val isSelectedDevice = contact.phoneNumber.trim() == activePhone.trim()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_item_${contact.id}"),
                    elevation = CardDefaults.cardElevation(0.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelectedDevice) Color(0xFFE0F7FA) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelectedDevice) 2.dp else 1.dp,
                        color = if (isSelectedDevice) Color(0xFF00ACC1) else BentoBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.updateProfilePhone(contact.phoneNumber)
                                    Toast.makeText(context, "Switched device simulation to: ${contact.name}", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoOnSurface
                                )
                                if (isSelectedDevice) {
                                    Surface(
                                        shape = RoundedCornerShape(100.dp),
                                        color = Color(0xFF00ACC1)
                                    ) {
                                        Text(
                                            text = "Simulating",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ph: " + contact.phoneNumber,
                                fontSize = 12.sp,
                                color = BentoOnSurface.copy(alpha = 0.6f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (contact.isInnerCircle) "Inner Circle" else "General",
                                    fontSize = 11.sp,
                                    fontWeight = if (contact.isInnerCircle) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = if (contact.isInnerCircle) Color(0xFF00796B) else BentoOnSurface.copy(alpha = 0.5f)
                                )
                                Switch(
                                    checked = contact.isInnerCircle,
                                    onCheckedChange = { viewModel.toggleInnerCircle(contact) },
                                    thumbContent = {
                                        if (contact.isInnerCircle) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize)
                                            )
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF00796B)
                                    ),
                                    modifier = Modifier.scale(0.8f).testTag("contact_switch_${contact.id}")
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteContact(contact.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete contact",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    android.graphics.Matrix().apply { setScale(scale, scale) }.let { Modifier }
)

@Composable
fun ViewWebsiteScreen(
    websiteId: Int,
    viewModel: MainViewModel
) {
    val websites by viewModel.allWebsites.collectAsState()
    val website = websites.firstOrNull { it.id == websiteId }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (website == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Website draft not found.")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            shadowElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { viewModel.activeScreen.value = Screen.Builder }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = website.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 160.dp)
                        )
                        Text(
                            text = "Created via Gemini",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(website.htmlContent))
                            Toast.makeText(context, "Full HTML/CSS code copied to clipboard! 🚀", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy HTML", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.activeScreen.value = Screen.Builder },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("All Drafts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            HtmlWebView(
                htmlContent = website.htmlContent,
                modifier = Modifier.fillMaxSize().testTag("website_rendered_webview")
            )
        }
    }
}

@Composable
fun ProfileSwitcherDialog(
    currentPhone: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    viewModel: MainViewModel
) {
    var phoneInput by remember { mutableStateOf(currentPhone) }
    val contacts by viewModel.allContacts.collectAsState()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Simulated Profile Check", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Input a mock phone number to test our paywall rules. E.g.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Simulated Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("phone_switcher_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Quick Select Seed Contacts:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.heightIn(max = 140.dp)
                ) {
                    items(contacts) { contact ->
                        Surface(
                            onClick = {
                                phoneInput = contact.phoneNumber
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            color = if (phoneInput == contact.phoneNumber) Color(0xFFF0FDFA) else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(contact.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(contact.phoneNumber, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                if (contact.isInnerCircle) {
                                    Surface(
                                        color = Color(0xFFCCFBF1),
                                        modifier = Modifier.padding(2.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("VIP Exemption", fontSize = 8.sp, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                    }
                                } else {
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        modifier = Modifier.padding(2.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Standard Plan", fontSize = 8.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(phoneInput) },
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Confirm Simulation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isInner by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Contact Entry", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name") },
                    placeholder = { Text("E.g., Mercy Wanjiku") },
                    modifier = Modifier.fillMaxWidth().testTag("add_contact_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("E.g. 0712345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("add_contact_phone_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Inner Circle Exemption", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Switch(
                        checked = isInner,
                        onCheckedChange = { isInner = it },
                        modifier = Modifier.testTag("add_contact_inner_switch")
                    )
                }
                Text(
                    text = "Toggling exemption unlocks the full website creator immediately without requiring payment.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAdd(name, phone, isInner)
                    }
                },
                modifier = Modifier.testTag("submit_contact_button")
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PaywallAndMpesaDialog(
    currentPhone: String,
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    var paymentStep by remember { mutableStateOf(1) } // 1: Initiator, 2: PIN Prompt, 3: SSL Handshake
    var phoneNumberInput by remember { mutableStateOf(currentPhone) }
    var pinCodeInput by remember { mutableStateOf("") }
    var checkoutStatusText by remember { mutableStateOf("") }
    var generatedTxId by remember { mutableStateOf("") }
    
    val showSuccessAlert by viewModel.showPaymentSuccessAlert.collectAsState()
    val context = LocalContext.current

    // Launch secure simulation when step 3 is triggered
    if (paymentStep == 3) {
        LaunchedEffect(Unit) {
            checkoutStatusText = "Establishing secure SSL connection to Safaricom..."
            kotlinx.coroutines.delay(1000)
            checkoutStatusText = "Handshaking encrypted Daraja API payload..."
            kotlinx.coroutines.delay(1000)
            checkoutStatusText = "Verifying STK callback signature match..."
            kotlinx.coroutines.delay(800)
            
            val randomChars = ('A'..'Z') + ('0'..'9')
            val code = "MK" + (1..8).map { randomChars.random() }.joinToString("")
            generatedTxId = code
            
            viewModel.currentUserPhone.value = phoneNumberInput.trim()
            viewModel.mpesaTxInput.value = code
            viewModel.processMpesaPayment()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!showSuccessAlert && paymentStep != 3) {
                onDismiss()
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (showSuccessAlert) BentoMpesaGreen else BentoPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showSuccessAlert) Icons.Default.Check else Icons.Default.Payment, 
                        contentDescription = null, 
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = if (showSuccessAlert) "Payment Secured!" else "M-Pesa Checkout Flow", 
                    fontWeight = FontWeight.Bold,
                    color = BentoSecondary,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (showSuccessAlert) {
                    // Step 4: Beautiful secure digital payment receipt
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle, 
                                contentDescription = null, 
                                tint = BentoMpesaGreen, 
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        
                        Text(
                            text = "KES 50.00 SUCCESSFUL",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoMpesaGreen
                        )
                        
                        // Styled receipt card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BentoSurface.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, BentoBorder),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Merchant Ref", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoSecondary)
                                    Text("0715871815 (Host/Website)", fontSize = 11.sp, color = BentoOnSurface)
                                }
                                HorizontalDivider(color = BentoBorder.copy(alpha = 0.3f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Payer Customer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoSecondary)
                                    Text(phoneNumberInput, fontSize = 11.sp, color = BentoOnSurface)
                                }
                                HorizontalDivider(color = BentoBorder.copy(alpha = 0.3f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("M-Pesa Receipt ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoSecondary)
                                    Text(
                                        text = if (generatedTxId.isNotEmpty()) generatedTxId else "MKF73ED92W", 
                                        fontFamily = FontFamily.Monospace, 
                                        fontSize = 11.sp, 
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BentoPrimary
                                    )
                                }
                                HorizontalDivider(color = BentoBorder.copy(alpha = 0.3f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Billing Cycle", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoSecondary)
                                    Text("Weekly Subscription (KES 50)", fontSize = 11.sp, color = BentoOnSurface)
                                }
                                HorizontalDivider(color = BentoBorder.copy(alpha = 0.3f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Gate Protocol", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoSecondary)
                                    Text("🔒 AES-256 SSL Encrypted", fontSize = 11.sp, color = BentoMpesaGreen, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        
                        Text(
                            text = "Thank you! Unlimited design drafts on Website Creator are now unlocked for your active simulated phone key.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = BentoOnSurface.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                } else if (paymentStep == 1) {
                    // Step 1: Initiator interface
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser, 
                                contentDescription = null, 
                                tint = BentoMpesaGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Safaricom Daraja Integrated Merchant",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Text(
                        text = "To authorize unlimited HTML website creations: you can trigger a simulated secure M-Pesa STK Push payment of KES 50 directly below.",
                        fontSize = 13.sp,
                        color = BentoOnSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = phoneNumberInput,
                        onValueChange = { phoneNumberInput = it },
                        label = { Text("Billing Phone Number (Safaricom)") },
                        placeholder = { Text("E.g., 0715871815") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("billing_phone_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoOnSurface,
                            unfocusedTextColor = BentoOnSurface,
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder
                        )
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = BentoSurface),
                        border = BorderStroke(1.dp, BentoBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "TRANSACTION ORDER CONFIGURATION:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "• CHARGE TARIFF: KES 50 (Weekly Access Plan)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSecondary
                            )
                            Text(
                                text = "• MERCHANT RECIPIENT: 0715871815 (Website Creator)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSecondary
                            )
                            Text(
                                text = "• SECURITY CHECK: PIN-controlled customer authorization",
                                fontSize = 11.sp,
                                color = BentoOnSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else if (paymentStep == 2) {
                    // Step 2: Realistic Overlay SIM Toolkit STK Push Dialog
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
                        border = BorderStroke(2.dp, Color(0xFF9E9E9E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .testTag("stk_push_overlay_dialog")
                    ) {
                        Column {
                            // Title bar of SIM Menu
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFBDBDBD))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "M-PESA ONLINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black
                                )
                            }
                            
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Do you want to pay KES 50.00 to Website Creator (0715871815)?",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                
                                Text(
                                    text = "Enter M-PESA PIN:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray
                                )
                                
                                OutlinedTextField(
                                    value = pinCodeInput,
                                    onValueChange = { if (it.length <= 4) pinCodeInput = it },
                                    placeholder = { Text("••••") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("stk_pin_input_field"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedBorderColor = BentoMpesaGreen,
                                        unfocusedBorderColor = Color.DarkGray
                                    )
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { paymentStep = 1 },
                                        modifier = Modifier.testTag("stk_cancel_button")
                                    ) {
                                        Text("CANCEL", color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = {
                                            if (pinCodeInput.length == 4) {
                                                paymentStep = 3
                                            } else {
                                                Toast.makeText(context, "Please enter your 4-digit M-PESA PIN", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoMpesaGreen),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.testTag("stk_pay_now_button")
                                    ) {
                                        Text("PAY NOW", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else if (paymentStep == 3) {
                    // Step 3: Secure Network Gateway Progress Screen
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("secure_checkout_loading"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = BentoMpesaGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = checkoutStatusText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSecondary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Please do not close this window. Intersecting secure Daraja nodes...",
                            fontSize = 10.sp,
                            color = BentoOnSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (showSuccessAlert) {
                Button(
                    onClick = {
                        viewModel.showPaymentSuccessAlert.value = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoMpesaGreen),
                    modifier = Modifier.fillMaxWidth().testTag("receipt_done_button")
                ) {
                    Text("Enter Builder & Generate", fontWeight = FontWeight.Bold)
                }
            } else if (paymentStep == 1) {
                Button(
                    onClick = {
                        if (phoneNumberInput.trim().length >= 9) {
                            paymentStep = 2
                        } else {
                            Toast.makeText(context, "Please enter a valid Safaricom Phone Number.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("initiate_stk_button")
                ) {
                    Text("Trigger Secure STK Push", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!showSuccessAlert && paymentStep == 1) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_paywall_button")
                ) {
                    Text("Cancel", color = BentoSecondary)
                }
            }
        }
    )
}

@Composable
fun HtmlWebView(htmlContent: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}
