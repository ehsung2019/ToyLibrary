package com.toylibrary.app.ui.theme

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import android.content.Context
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.toylibrary.app.profile.ProfileScreen
import com.toylibrary.app.ai.removeBackground
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.CircularProgressIndicator
import com.toylibrary.app.ai.detectToyName
import com.toylibrary.app.Toy
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.provider.OpenableColumns
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.exifinterface.media.ExifInterface
import android.graphics.Matrix
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import com.toylibrary.app.auth.saveLoginState
import com.toylibrary.app.billing.BillingManager
import com.toylibrary.app.data.GeminiRepository
import com.toylibrary.app.data.UserToy
import android.net.Uri
import androidx.compose.runtime.*
import com.toylibrary.app.ai.detectToyAge
import com.toylibrary.app.ai.detectToyInfo
import kotlinx.coroutines.coroutineScope
import com.toylibrary.app.R
import com.toylibrary.app.toyList

fun getDueDate(borrowedAt: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = borrowedAt
        add(Calendar.DAY_OF_YEAR, 7)
    }
    return SimpleDateFormat("MMM d", Locale.getDefault())
        .format(calendar.time)
}


@Composable
fun FeaturedToysRow(
    toys: List<Toy>,
    onToyClick: (Toy) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(end = 32.dp)
    ) {
        items(toys) { toy ->
            FeaturedToyCard(
                toy = toy,
                onClick = onToyClick
            )
        }
    }
}


@Composable
fun HomeContent(
    loanedToys: MutableList<Toy>,
    onToyClick: (Toy) -> Unit
) {
    var searchResults by remember { mutableStateOf(toyList) }
    var isSearching by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            SearchBar(
                allToys = toyList,
                onSearchResult = { result, searching ->
                    searchResults = result
                    isSearching = searching
                }
            )
        }

        if (isSearching) {
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { SectionTitle("Search Results") }

            items(searchResults) { toy ->
                ToyListItem(
                    toy = toy,
                    isBorrowed = loanedToys.contains(toy),
                    onBorrowClick = {
                        if (!loanedToys.contains(toy)) {
                            loanedToys.add(toy)
                        }
                    }
                )
            }
        } else {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { SectionTitle("Featured Toys") }

            item {
                FeaturedToysRow(
                    toys = toyList,
                    onToyClick = onToyClick
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { SectionTitle("Categories") }

            item { CategoriesRow() }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { SectionTitle("My Loans") }

            if (loanedToys.isEmpty()) {
                item { EmptyLoansState() }
            } else {
                items(loanedToys) { toy ->
                    LoanItem(
                        toy = toy,
                        onReturnClick = {
                            loanedToys.remove(toy)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyLoansState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No toys borrowed yet",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Browse toys and borrow your favorites!",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}



@Composable
fun SearchBar(
    allToys: List<Toy>,
    onSearchResult: (List<Toy>, Boolean) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = {
            query = it

            val filteredToys = if (query.isBlank()) {
                allToys
            } else {
                allToys.filter { toy ->
                    toy.name.contains(query, ignoreCase = true)
                }
            }

            onSearchResult(filteredToys, query.isNotBlank())
        },
        placeholder = { Text("Search toys...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        )
    )
}
@Composable
fun FeaturedToyCard(
    toy: Toy,
    onClick: (Toy) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick(toy) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            toy.imageRes?.let { res ->
                Image(
                    painter = painterResource(res),
                    contentDescription = toy.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(toy.name, fontWeight = FontWeight.Bold)
            Text(toy.age, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CategoryItem(
    name: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.size(88.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CategoriesRow() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {

        CategoryItem("STEM", Icons.Default.Science)
        CategoryItem("Board Games", Icons.Default.Casino)
        CategoryItem("Outdoor", Icons.Default.Park)
        CategoryItem("Educational", Icons.Default.MenuBook)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(isLoggedIn: Boolean,
               onLoginRequired: () -> Unit) {
    var selectedItem by remember { mutableStateOf(0) }

    val loanedToys = remember {
        mutableStateListOf<Toy>()
    }

    val communityToys = remember {
        mutableStateListOf<UserToy>()
    }

    var selectedToy by remember { mutableStateOf<Toy?>(null) }

    var showGemini by remember { mutableStateOf(false) }

    var showUpgrade by remember { mutableStateOf(false) }

    var showLogoutDialog by remember { mutableStateOf(false) }

    var showPostToy by remember { mutableStateOf(false) }

    val items = listOf("Home", "Browse", "Alerts", "My Loans", "Profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Search,
        Icons.Default.Notifications,   // Alerts
        Icons.Default.ShoppingCart,    // My Loans
        Icons.Default.AccountCircle
    )

    val activity = LocalContext.current as Activity

    val billingManager = remember(activity) {
        BillingManager(
            activity = activity,
            onPurchaseSuccess = {
                // refresh UI / unlock premium
            }
        )
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onProfileClick = {
                    selectedItem = 4
                }
            )
        },
        bottomBar = {
            Column {
                AdBanner()   // 👈 Banner ad ABOVE navigation

                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = {
                                showPostToy = false
                                selectedToy = null
                                selectedItem = index
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedToy == null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Show Post Toy FAB ONLY when not already on PostToyScreen
                    if (!showPostToy) {
                        FloatingActionButton(
                            onClick = { showPostToy = true }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Post Toy")
                        }
                    }

                    // Always show Gemini unless inside ToyDetailScreen
                    GeminiFab(
                        onClick = { showGemini = true }
                    )
                }
            }
        }
    )
    { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {

            // Dialogs can still overlay
            if (showGemini) {
                GeminiDialog(
                    onDismiss = { showGemini = false }
                )
            }

            if (showUpgrade) {
                UpgradeDialog(
                    onDismiss = { showUpgrade = false }
                )
            }

            if (showLogoutDialog) {
                LogoutDialog(
                    onDismiss = {
                        showLogoutDialog = false
                        onLoginRequired()
                    }
                )
            }

            if (showPostToy) {
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )

                ModalBottomSheet(
                    onDismissRequest = { showPostToy = false },
                    sheetState = sheetState
                ) {
                    PostToyScreen(
                        onPost = { toy ->
                            communityToys.add(toy)
                            showPostToy = false
                        },
                        onClose = {
                            showPostToy = false
                        }
                    )
                }
            }
            // Main content switching (full-screen behavior, not overlay)
            when {
                selectedToy != null -> {
                    ToyDetailScreen(
                        toy = selectedToy!!,
                        onBorrowClick = {
                            selectedToy?.let { toy ->
                                if (!loanedToys.contains(toy)) {
                                    toy.borrowedAt = System.currentTimeMillis()
                                    loanedToys.add(toy)
                                }
                            }
                            selectedToy = null
                        }
                    )
                }

                else -> {
                    when {
                        selectedToy != null -> {
                            ToyDetailScreen(
                                toy = selectedToy!!,
                                onBorrowClick = {
                                    selectedToy?.let { toy ->
                                        if (!loanedToys.contains(toy)) {
                                            toy.borrowedAt = System.currentTimeMillis()
                                            loanedToys.add(toy)
                                        }
                                    }
                                    selectedToy = null
                                }
                            )
                        }

                        else -> {
                            when (selectedItem) {
                                0 -> HomeContent(
                                    loanedToys = loanedToys,
                                    onToyClick = { toy ->
                                        selectedToy = toy
                                    }
                                )

                                1 -> BrowseScreen(
                                    loanedToys = loanedToys,
                                    communityToys = communityToys,
                                    onBorrow = { toy ->
                                        if (!isLoggedIn) {
                                            onLoginRequired()
                                            return@BrowseScreen
                                        }

                                        if (!loanedToys.contains(toy)) {
                                            loanedToys.add(toy)
                                        }
                                    }
                                )

                                2 -> NotificationsScreen()
                                3 -> MyLoansScreen(loanedToys)

                                4 -> ProfileScreen(
                                    onUpgradeClick = { showUpgrade = true },
                                    onLogout = { showLogoutDialog = true },
                                    billingManager = billingManager   // ✅ ADD THIS
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityToysScreen(
    toys: List<UserToy>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(toys) { toy ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    toy.imageUri?.let { imageUri ->
                        AsyncImage(
                            model = imageUri,
                            contentDescription = toy.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(Modifier.height(12.dp))
                    }

                    Text(
                        toy.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Text(toy.age)

                    Spacer(Modifier.height(4.dp))

                    Text(toy.description)

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Shared by ${toy.owner}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PostToyScreen(
    onPost: (UserToy) -> Unit,
    onClose: () -> Unit
) {
    var enhancedMode by remember { mutableStateOf(false) }


    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        selectedImageUri = uri

        uri?.let {
            coroutineScope.launch {

                isAnalyzing = true   // ⭐ start loading

                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap != null) {

                    val rotated = rotateBitmapIfRequired(context, it, bitmap)
                    val resized = resizeImage(rotated)
                    val enhanced = enhanceToyImage(resized)

                    processedBitmap = enhanced

                    try {
                        val toyInfo = detectToyInfo(context, it)

                        name = toyInfo.name
                        age = toyInfo.age
                        description = toyInfo.description

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                isAnalyzing = false   // ⭐ stop loading
            }
        }
    }
    val scrollState = rememberScrollState()

    val nameRequester = remember { BringIntoViewRequester() }
    val ageRequester = remember { BringIntoViewRequester() }
    val descriptionRequester = remember { BringIntoViewRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding() // 👈 important for keyboard
            .padding(16.dp)
    ) {
        Text(
            text = "Share Your Toy",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // Image preview area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable {
                    imagePickerLauncher.launch("image/*")
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (processedBitmap != null) {

                    Image(
                        bitmap = processedBitmap!!.asImageBitmap(),
                        contentDescription = "Toy photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )

                } else if (selectedImageUri != null) {

                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected toy photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tap to upload toy photo",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                imagePickerLauncher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (selectedImageUri == null) "Upload Toy Photo" else "Change Photo")
        }

        Spacer(Modifier.height(16.dp))

        if (isAnalyzing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.width(8.dp))
                Text("Analyzing toy with AI...")
            }
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Toy Name") },
            enabled = !isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(nameRequester)
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            nameRequester.bringIntoView()
                            selectedImageUri?.let {

                            }
                        }
                    }
                },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age Range") },
            enabled = !isAnalyzing, // ⭐ disable while AI analyzing
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(ageRequester)
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            ageRequester.bringIntoView()
                        }
                    }
                },
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            enabled = !isAnalyzing, // ⭐ disable while AI analyzing
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(descriptionRequester)
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            descriptionRequester.bringIntoView()
                        }
                    }
                },
            minLines = 3
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val toy = UserToy(
                    name = name,
                    age = age,
                    description = description,
                    owner = "User",
                    imageUri = selectedImageUri?.toString()
                )
                onPost(toy)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && age.isNotBlank() && description.isNotBlank()
        ) {
            Text("Share Toy")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }

        Spacer(Modifier.height(24.dp)) // 👈 extra bottom breathing room
    }
}

fun enhanceToyImage(bitmap: Bitmap): Bitmap {

    val width = bitmap.width
    val height = bitmap.height

    val enhanced = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)

    for (x in 0 until width) {
        for (y in 0 until height) {

            val pixel = bitmap.getPixel(x, y)

            val r = (android.graphics.Color.red(pixel) * 1.1).toInt().coerceAtMost(255)
            val g = (android.graphics.Color.green(pixel) * 1.1).toInt().coerceAtMost(255)
            val b = (android.graphics.Color.blue(pixel) * 1.1).toInt().coerceAtMost(255)

            enhanced.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
        }
    }

    return enhanced
}

fun resizeImage(bitmap: Bitmap): Bitmap {

    val maxSize = 1024

    val ratio = minOf(
        maxSize.toFloat() / bitmap.width,
        maxSize.toFloat() / bitmap.height
    )

    val width = (bitmap.width * ratio).toInt()
    val height = (bitmap.height * ratio).toInt()

    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null

    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                name = it.getString(nameIndex)
            }
        }
    }

    if (name == null) {
        name = uri.path?.substringAfterLast('/')
    }

    return name
}
fun suggestToyNameFromUri(context: Context, uri: Uri): String {
    val fileName = getFileName(context, uri)?.lowercase() ?: return "Toy"

    return when {
        "bear" in fileName -> "Teddy Bear"
        "lego" in fileName -> "LEGO Set"
        "car" in fileName -> "Toy Car"
        "truck" in fileName -> "Toy Truck"
        "doll" in fileName -> "Doll"
        "puzzle" in fileName -> "Puzzle"
        "train" in fileName -> "Toy Train"
        "block" in fileName -> "Building Blocks"
        "robot" in fileName -> "Toy Robot"
        "ball" in fileName -> "Ball"
        else -> "Toy"
    }
}
@Composable
fun LogoutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Logged Out") },
        text = { Text("User logged out successfully.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun UpgradeDialog(
    onDismiss: () -> Unit
) {
    val activity = LocalContext.current as? Activity



    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upgrade Membership ⭐",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enjoy premium benefits instantly — no toy posting required:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                PremiumBenefitRow("Remove Ads", Icons.Default.Block)
                PremiumBenefitRow("AI toy recommendations", Icons.Default.AutoAwesome)
                PremiumBenefitRow("Bonus reward points every month", Icons.Default.Redeem)

                Spacer(modifier = Modifier.height(20.dp))

                if (activity != null) {
                    val billingManager = remember(activity) {
                        BillingManager(
                            activity = activity,
                            onPurchaseSuccess = {
                                onDismiss()
                            }
                        )
                    }

                    // Monthly Plan Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Monthly Plan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Flexible and affordable",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    billingManager.startConnection {
                                        billingManager.launchPurchase(
                                            productId = "premium_monthly"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Choose Monthly")
                            }
                        }
                    }

                    // Yearly Plan Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Yearly Plan ⭐ Best Value",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Save more with annual billing",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    billingManager.startConnection {
                                        billingManager.launchPurchase(
                                            productId = "premium_yearly"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Choose Yearly")
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Unable to load purchase options right now.",
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now")
            }
        }
    )
}

@Composable
fun PremiumBenefitRow(
    text: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}
@Composable
fun MyLoansScreen(
    loanedToys: MutableList<Toy>
) {
    if (loanedToys.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No toys borrowed yet",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(loanedToys) { toy ->
                LoanItem(
                    toy = toy,
                    onReturnClick = {
                        loanedToys.remove(toy)
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationsScreen() {
    val alerts = listOf(
        "Your toy 'LEGO Robot Kit' is due in 2 days",
        "New STEM toys added to the library",
        "You earned 10 reward points 🎉",
        "Reminder: Return toys on time to avoid penalties"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Notifications",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(alerts) { alert ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = alert,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


@Composable
fun ProfileItem(
    title: String,
    onClick: (() -> Unit)? = null,
    isDestructive: Boolean = false,
    highlight: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    isDestructive -> Color.Red
                    highlight -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = { Text("ToyLibrary", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { /* menu later */ }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
            }
        }
    )
}



@Composable
fun LoanItem(
    toy: Toy,
    onReturnClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Image should be fixed size, NOT fillMaxWidth
            toy.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = toy.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } ?: toy.imageRes?.let { res ->
                Image(
                    painter = painterResource(res),
                    contentDescription = toy.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = toy.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = toy.age,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                toy.borrowedAt?.let {
                    Text(
                        text = "Due: ${getDueDate(it)}",
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = onReturnClick) {
                Text("Return")
            }
        }
    }
}

@Composable
fun BrowseScreen(
    loanedToys: MutableList<Toy>,
    communityToys: List<UserToy>,
    onBorrow: (Toy) -> Unit
) {

    val allToys = toyList + communityToys.map {
        Toy(
            name = it.name,
            age = it.age,
            imageRes = null,
            imageUri = it.imageUri,
            description = it.description
        )
    }

    var filteredToys by remember { mutableStateOf(allToys) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            SearchBar(
                allToys = allToys,
                onSearchResult = { result, _ ->
                    filteredToys = result
                }
            )
        }
        item { FilterChips() }

        items(filteredToys) { toy ->    ToyListItem(
                toy = toy,
                isBorrowed = loanedToys.contains(toy),
                onBorrowClick = {
                    onBorrow(toy)
                }
            )
        }
    }
}

@Composable
fun FilterChips() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        items(listOf("All", "STEM", "Educational", "Board Games")) {
            AssistChip(
                onClick = {},
                label = { Text(it) }
            )
        }
    }
}

@Composable
fun ToyListItem(
    toy: Toy,
    isBorrowed: Boolean,
    onBorrowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            toy.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = toy.name,
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Fit
                )
            } ?: toy.imageRes?.let { res ->
                Image(
                    painter = painterResource(res),
                    contentDescription = toy.name,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(toy.name, fontWeight = FontWeight.Bold)
                Text(toy.age)
                Text(
                    if (isBorrowed) "Borrowed" else "Available",
                    color = if (isBorrowed) Color.Red else Color(0xFF4CAF50),
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onBorrowClick,
                enabled = !isBorrowed
            ) {
                Text(if (isBorrowed) "Borrowed" else "Borrow")
            }
        }
    }
}

@Composable
fun ToyDetailScreen(
    toy: Toy,
    onBorrowClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // --- Image section ---
        toy.imageRes?.let { res ->
            Image(
                painter = painterResource(res),
                contentDescription = toy.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Fit
            )
        }


        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // --- Title + Rating ---
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = toy.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107)
                )
                Text("4.5", fontSize = 14.sp)
                Text(" (23)", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Chips ---
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("Ages 3+") }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Available") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Description ---
            Text(
                text = "Description",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = toy.description,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Borrowing Info ---
            Text(
                text = "Borrowing Information",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(
                icon = Icons.Default.DateRange,
                text = "Loan Duration: 7 days"
            )
            InfoRow(
                icon = Icons.Default.Warning,
                text = "Late Penalty: -1 reward/day"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Return within 7 days to avoid penalties",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Borrow Button ---
            Button(
                onClick = onBorrowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Borrow Toy", fontSize = 16.sp)
            }

        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}

@Composable
fun GeminiFab(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Ask Gemini",
            tint = Color.White
        )
    }
}

@Composable
fun GeminiDialog(
    onDismiss: () -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<String>() }
    var isThinking by remember { mutableStateOf(false) }
    var pendingQuestion by remember { mutableStateOf<String?>(null) }

    // ✅ One LaunchedEffect only
    LaunchedEffect(pendingQuestion) {
        pendingQuestion?.let { question ->
            val repository = GeminiRepository()

            try {
                val reply = repository.askGemini(question)
                messages.add("Gemini: $reply")
            } catch (e: Exception) {
                messages.add("Gemini error: ${e.message}")
                e.printStackTrace()
            }

            isThinking = false
            pendingQuestion = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ask Gemini 🤖") },
        text = {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(scrollState)
            ) {

                messages.forEach {
                    Text(it, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                }

                if (isThinking) {
                    Text("Gemini: thinking…", fontSize = 13.sp)
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Ask a question…") },
                    enabled = !isThinking
                )
            }
        },
        confirmButton = {
            Button(
                enabled = userInput.isNotBlank() && !isThinking,
                onClick = {
                    val question = userInput
                    userInput = ""

                    messages.add("You: $question")
                    isThinking = true
                    pendingQuestion = question
                }
            ) {
                Text("Send")
            }
        },
        dismissButton = {                     // 👈 ADD THIS
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

}

fun rotateBitmapIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {

    val inputStream = context.contentResolver.openInputStream(uri)
    val exif = inputStream?.let { ExifInterface(it) }

    val orientation = exif?.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    val matrix = Matrix()

    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        else -> return bitmap
    }

    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}