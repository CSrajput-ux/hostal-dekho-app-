package com.company.hostaldekho.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.company.hostaldekho.util.IndiaLocations
import com.company.hostaldekho.data.remote.dto.PropertyCreateRequest
import com.company.hostaldekho.data.remote.dto.RoomCreateRequest
import com.company.hostaldekho.ui.theme.*
import com.company.hostaldekho.ui.viewmodels.PropertyViewModel
import com.company.hostaldekho.ui.viewmodels.SubmitPropertyState
import java.math.BigDecimal

// ─────────────────────────────────────────────────────────────────────────────
// Data classes holding form state per step
// ─────────────────────────────────────────────────────────────────────────────

data class PropertyStep1Data(
    val propertyType: String = "",       // "HOSTEL", "PG", "FLAT"
    val gender: String = "",             // "BOYS", "GIRLS", "UNISEX"
    val hostelName: String = "",
    val hostelLocation: String = "",     // area/landmark description
    val houseNumber: String = "",
    val street: String = "",
    val city: String = "",
    val pincode: String = "",
    val state: String = "",
    val otherDetails: String = ""
) {
    fun isValid(): Boolean {
        return propertyType.isNotBlank() &&
               gender.isNotBlank() &&
               hostelName.isNotBlank() &&
               hostelLocation.isNotBlank() &&
               houseNumber.isNotBlank() &&
               street.isNotBlank() &&
               city.isNotBlank() &&
               pincode.isNotBlank() &&
               state.isNotBlank()
    }
}

data class PropertyStep2Data(
    val selectedFacilities: Set<String> = emptySet()
)

data class RoomData(
    val roomType: String = "Single",
    val pricePerMonth: String = "",
    val availableCount: String = "1"
)

data class PropertyStep3Data(
    val rooms: List<RoomData> = listOf(RoomData())
) {
    fun isValid(): Boolean = rooms.isNotEmpty() && rooms.all {
        it.roomType.isNotBlank() && it.pricePerMonth.isNotBlank() &&
        it.pricePerMonth.toDoubleOrNull() != null && it.availableCount.toIntOrNull() != null
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(
    onBack: () -> Unit,
    viewModel: PropertyViewModel = hiltViewModel()
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var step1Data by remember { mutableStateOf(PropertyStep1Data()) }
    var step2Data by remember { mutableStateOf(PropertyStep2Data()) }
    var step3Data by remember { mutableStateOf(PropertyStep3Data()) }

    val submitState by viewModel.submitPropertyState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val isNextEnabled = when (currentStep) {
        1 -> step1Data.isValid()
        2 -> true   // Facilities are optional
        3 -> step3Data.isValid()
        4 -> true   // Verification documents optional for now
        else -> true
    }

    // Handle submit result
    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is SubmitPropertyState.Success -> {
                snackbarHostState.showSnackbar("Property \"${state.property.name}\" submitted successfully!")
                viewModel.resetSubmitPropertyState()
                onBack()
            }
            is SubmitPropertyState.Error -> {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                viewModel.resetSubmitPropertyState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Property — Step $currentStep of 4",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 1) currentStep-- else onBack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = currentStep / 4f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(vertical = 16.dp)
                    .background(Color.Transparent, RoundedCornerShape(4.dp)),
                color = PrimaryBlue,
                trackColor = PrimaryBlue.copy(alpha = 0.1f)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                when (currentStep) {
                    1 -> Step1Address(data = step1Data, onDataChange = { step1Data = it })
                    2 -> Step2Facilities(
                        data = step2Data,
                        onDataChange = { step2Data = it }
                    )
                    3 -> Step3Rooms(data = step3Data, onDataChange = { step3Data = it })
                    4 -> Step4Verification()
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            val isLoading = submitState is SubmitPropertyState.Loading

            Button(
                onClick = {
                    if (currentStep < 4) {
                        currentStep++
                    } else {
                        // SUBMIT — build PropertyCreateRequest from all step data
                        val address = "${step1Data.houseNumber}, ${step1Data.street}, ${step1Data.hostelLocation}"
                        val amenitiesMap = step2Data.selectedFacilities.associateWith { true }

                        val roomRequests = step3Data.rooms.mapNotNull { room ->
                            val price = room.pricePerMonth.toDoubleOrNull() ?: return@mapNotNull null
                            val count = room.availableCount.toIntOrNull() ?: 1
                            RoomCreateRequest(
                                roomType = room.roomType,
                                price = BigDecimal.valueOf(price),
                                availabilityCount = count
                            )
                        }

                        viewModel.submitProperty(
                            PropertyCreateRequest(
                                name = step1Data.hostelName,
                                description = step1Data.otherDetails.ifBlank { null },
                                address = address,
                                city = step1Data.city,
                                state = step1Data.state,
                                pincode = step1Data.pincode,
                                type = step1Data.propertyType,    // Already uppercase: HOSTEL/PG/FLAT
                                gender = step1Data.gender,         // BOYS/GIRLS/UNISEX
                                amenities = if (amenitiesMap.isEmpty()) null else amenitiesMap,
                                rooms = roomRequests
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isNextEnabled && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (currentStep < 4) "NEXT" else "SUBMIT PROPERTY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1 — Address & Type
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Address(data: PropertyStep1Data, onDataChange: (PropertyStep1Data) -> Unit) {
    val propertyTypes = listOf("HOSTEL", "PG", "FLAT")
    val genderOptions = listOf("BOYS", "GIRLS", "UNISEX")
    var typeExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }

    val selectedStateObj = remember(data.state) {
        IndiaLocations.states.find { it.name.equals(data.state, ignoreCase = true) }
    }

    val availableDistricts = remember(selectedStateObj) {
        selectedStateObj?.districts ?: emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            "Property Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Property Type Dropdown
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
        ) {
            OutlinedTextField(
                value = data.propertyType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Property Type *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                propertyTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            onDataChange(data.copy(propertyType = type))
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        // Gender Dropdown
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = data.gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender Allowed *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
            ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                genderOptions.forEach { g ->
                    DropdownMenuItem(
                        text = { Text(g) },
                        onClick = {
                            onDataChange(data.copy(gender = g))
                            genderExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = data.hostelName,
            onValueChange = { onDataChange(data.copy(hostelName = it)) },
            label = { Text("Hostel/Property Name *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        OutlinedTextField(
            value = data.hostelLocation,
            onValueChange = { onDataChange(data.copy(hostelLocation = it)) },
            label = { Text("Area/Landmark (e.g. Near Coaching, Station) *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )

        Text(
            "Address & State/District (India)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 8.dp)
        )

        // State Dropdown (All 28 States & 8 Union Territories)
        ExposedDropdownMenuBox(
            expanded = stateExpanded,
            onExpandedChange = { stateExpanded = !stateExpanded }
        ) {
            OutlinedTextField(
                value = data.state,
                onValueChange = { onDataChange(data.copy(state = it)) },
                label = { Text("Select State/UT *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
            ExposedDropdownMenu(expanded = stateExpanded, onDismissRequest = { stateExpanded = false }) {
                IndiaLocations.states.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s.name) },
                        onClick = {
                            onDataChange(data.copy(state = s.name, city = s.districts.firstOrNull() ?: ""))
                            stateExpanded = false
                        }
                    )
                }
            }
        }

        // City/District Dropdown
        ExposedDropdownMenuBox(
            expanded = districtExpanded,
            onExpandedChange = { districtExpanded = !districtExpanded }
        ) {
            OutlinedTextField(
                value = data.city,
                onValueChange = { onDataChange(data.copy(city = it)) },
                label = { Text("City/District *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )
            if (availableDistricts.isNotEmpty()) {
                ExposedDropdownMenu(expanded = districtExpanded, onDismissRequest = { districtExpanded = false }) {
                    availableDistricts.forEach { dist ->
                        DropdownMenuItem(
                            text = { Text(dist) },
                            onClick = {
                                onDataChange(data.copy(city = dist))
                                districtExpanded = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = data.houseNumber,
            onValueChange = { onDataChange(data.copy(houseNumber = it)) },
            label = { Text("House/Plot Number *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = data.street,
            onValueChange = { onDataChange(data.copy(street = it)) },
            label = { Text("Street/Locality *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = data.pincode,
            onValueChange = { onDataChange(data.copy(pincode = it)) },
            label = { Text("Pincode *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = data.otherDetails,
            onValueChange = { onDataChange(data.copy(otherDetails = it)) },
            label = { Text("Other Details (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            minLines = 3
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 2 — Facilities/Amenities
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun Step2Facilities(data: PropertyStep2Data, onDataChange: (PropertyStep2Data) -> Unit) {
    val facilities = listOf("Wifi", "AC", "Meals", "Laundry", "Parking", "CCTV", "RO Water", "Attached Washroom", "Geyser", "Power Backup", "Study Room", "TV")
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Select Facilities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Select all amenities available at your property", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        facilities.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    val isSelected = item in data.selectedFacilities
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = if (isSelected) {
                                data.selectedFacilities - item
                            } else {
                                data.selectedFacilities + item
                            }
                            onDataChange(data.copy(selectedFacilities = updated))
                        },
                        label = { Text(item) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 3 — Rooms & Pricing
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3Rooms(data: PropertyStep3Data, onDataChange: (PropertyStep3Data) -> Unit) {
    val roomTypes = listOf("Single", "Double", "Triple", "Dormitory")

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Room & Pricing Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Add room types with pricing. You can add multiple room categories.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

        data.rooms.forEachIndexed { index, room ->
            var typeExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Room ${index + 1}", fontWeight = FontWeight.Bold, color = TextPrimary)
                        if (data.rooms.size > 1) {
                            IconButton(onClick = {
                                val updated = data.rooms.toMutableList().also { it.removeAt(index) }
                                onDataChange(data.copy(rooms = updated))
                            }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Remove room", tint = DangerRed)
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = room.roomType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Room Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            roomTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        val updated = data.rooms.toMutableList()
                                        updated[index] = room.copy(roomType = type)
                                        onDataChange(data.copy(rooms = updated))
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = room.pricePerMonth,
                            onValueChange = {
                                val updated = data.rooms.toMutableList()
                                updated[index] = room.copy(pricePerMonth = it)
                                onDataChange(data.copy(rooms = updated))
                            },
                            label = { Text("Price/Month (₹)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = room.availableCount,
                            onValueChange = {
                                val updated = data.rooms.toMutableList()
                                updated[index] = room.copy(availableCount = it)
                                onDataChange(data.copy(rooms = updated))
                            },
                            label = { Text("Available") },
                            modifier = Modifier.weight(0.6f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Add more rooms button
        OutlinedButton(
            onClick = {
                onDataChange(data.copy(rooms = data.rooms + RoomData()))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Another Room Type")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 4 — Verification Documents
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun Step4Verification() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Verification & Photos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Upload documents for faster verification (optional for now).", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        UploadBox("Upload Property Electricity Bill")
        UploadBox("Upload Current Agreement / Rent Deed")
        UploadBox("Upload Aadhar Card (Front & Back)")
        UploadBox("Upload Property Photos (Min 3)")
    }
}

@Composable
fun UploadBox(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
