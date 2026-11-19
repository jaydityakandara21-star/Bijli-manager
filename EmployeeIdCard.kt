package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmployeeProfile
import com.example.ui.theme.LocalBijliColors
import com.example.ui.theme.MonoFontFamily

@Composable
fun EmployeeIdCard(
    profile: EmployeeProfile,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sem = LocalBijliColors.current
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.65f),
                        secondary.copy(alpha = 0.55f),
                        sem.cardBorder
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("employee_id_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Official Department Header Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Official Emblem",
                        tint = secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "OFFICIAL EMPLOYEE ID • कर्मचारी पहचान पत्र",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = profile.department,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(sem.paidContainer.copy(alpha = 0.9f))
                        .border(1.dp, sem.paid.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified Official",
                        tint = sem.paid,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = profile.dutyStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = sem.paid,
                        maxLines = 1
                    )
                }
            }

            // Main ID Card Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo / Emblem Crest
                    Box(modifier = Modifier.size(64.dp)) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            primary.copy(alpha = 0.25f),
                                            secondary.copy(alpha = 0.18f)
                                        )
                                    )
                                )
                                .border(1.5.dp, primary.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.initials,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified",
                                tint = sem.paid,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = profile.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = profile.designation,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Badge,
                                contentDescription = "Employee ID",
                                tint = primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "ID: ${profile.employeeId}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = primary
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Complete Employee Details Grid
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    IdDetailRow(
                        icon = Icons.Filled.LocationOn,
                        label = "Sub-Division / Feeders",
                        value = profile.subDivision
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IdDetailRow(
                            icon = Icons.Filled.Apartment,
                            label = "Zone / Circle",
                            value = profile.officeZone,
                            modifier = Modifier.weight(1f)
                        )
                        IdDetailRow(
                            icon = Icons.Filled.Phone,
                            label = "Official Mobile",
                            value = profile.mobileNumber,
                            isMono = true,
                            modifier = Modifier.weight(0.85f)
                        )
                    }
                }

                // Bottom Barcode + Edit ID Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        EmployeeIdBarcode(
                            seed = profile.employeeId,
                            barColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            modifier = Modifier
                                .width(84.dp)
                                .height(22.dp)
                        )
                        Text(
                            text = profile.employeeId,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    TextButton(
                        onClick = onEditClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("edit_employee_id_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Employee ID",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit ID",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IdDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    isMono: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(14.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = if (isMono) {
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                },
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmployeeIdBarcode(
    seed: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val weights = remember(seed) {
        val hash = seed.hashCode().let { if (it == 0) 42 else it }
        List(22) { idx ->
            val v = ((hash ushr (idx % 16)) xor (idx * 7)) and 0x3
            when (v) {
                0 -> 1.2f
                1 -> 2.4f
                2 -> 1.6f
                else -> 3.0f
            }
        }
    }

    Canvas(modifier = modifier) {
        val totalUnits = weights.sum() + weights.size * 1.4f
        val unitWidth = size.width / totalUnits
        var x = 0f
        weights.forEach { w ->
            val barW = w * unitWidth
            drawRect(
                color = barColor,
                topLeft = Offset(x, 0f),
                size = Size(barW, size.height)
            )
            x += barW + 1.4f * unitWidth
        }
    }
}

@Composable
fun EditEmployeeIdDialog(
    currentProfile: EmployeeProfile,
    onDismiss: () -> Unit,
    onSave: (EmployeeProfile) -> Unit
) {
    var fullName by remember(currentProfile) { mutableStateOf(currentProfile.fullName) }
    var employeeId by remember(currentProfile) { mutableStateOf(currentProfile.employeeId) }
    var designation by remember(currentProfile) { mutableStateOf(currentProfile.designation) }
    var department by remember(currentProfile) { mutableStateOf(currentProfile.department) }
    var subDivision by remember(currentProfile) { mutableStateOf(currentProfile.subDivision) }
    var officeZone by remember(currentProfile) { mutableStateOf(currentProfile.officeZone) }
    var mobileNumber by remember(currentProfile) { mutableStateOf(currentProfile.mobileNumber) }
    var dutyStatus by remember(currentProfile) { mutableStateOf(currentProfile.dutyStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Official Employee ID Card") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Employee Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = employeeId,
                    onValueChange = { employeeId = it },
                    label = { Text("Employee ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation (Pad)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department / Division") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subDivision,
                    onValueChange = { subDivision = it },
                    label = { Text("Sub-Division / Feeders") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = officeZone,
                    onValueChange = { officeZone = it },
                    label = { Text("Zone / Circle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { if (it.length <= 10 && it.all { ch -> ch.isDigit() }) mobileNumber = it },
                    label = { Text("Official Mobile (10-digit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dutyStatus,
                    onValueChange = { dutyStatus = it },
                    label = { Text("Duty Status") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        EmployeeProfile(
                            fullName = fullName.ifBlank { currentProfile.fullName },
                            employeeId = employeeId.ifBlank { currentProfile.employeeId },
                            designation = designation.ifBlank { currentProfile.designation },
                            department = department.ifBlank { currentProfile.department },
                            subDivision = subDivision.ifBlank { currentProfile.subDivision },
                            mobileNumber = mobileNumber.ifBlank { currentProfile.mobileNumber },
                            officeZone = officeZone.ifBlank { currentProfile.officeZone },
                            dutyStatus = dutyStatus.ifBlank { currentProfile.dutyStatus }
                        )
                    )
                },
                modifier = Modifier.testTag("save_employee_id_button")
            ) {
                Text("Save ID Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
