package com.example.ui.components.pc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.strings.AppStrings
import com.example.ui.theme.JetBrainsMonoFontFamily

private val Win11Blue = Color(0xFF0078D4)
private val Win11Sky = Color(0xFF60CDFF)

@Composable
fun WebAppRuntimeWindowBody(
    report: WebAppLaunchReport?,
    strings: AppStrings,
    onRetryExternal: (WebAppLaunchReport) -> Unit,
    onSearchRuntimeInChrome: (String) -> Unit,
    onOpenDownloadsFolder: () -> Unit
) {
    if (report == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().testTag("win11_webapp_runtime_body")
        ) {
            Text(strings.winNoExeHint, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val isHtmlMistake = report.headerInspection.contains("HTML", ignoreCase = true)
    val badgeColor = when {
        report.dispatchedExternally -> Color(0xFF4ADE80)
        isHtmlMistake -> Color(0xFFFF6B6B)
        else -> Color(0xFFFBBF24)
    }

    val runtimeSearchUrl = when (report.extension.lowercase()) {
        "jar" -> "https://www.google.com/search?q=PojavLauncher+Android+GitHub"
        else -> "https://www.google.com/search?q=Winlator+Android+GitHub+releases"
    }

    val scrollState = rememberScrollState()
    androidx.compose.runtime.LaunchedEffect(report.appName, report.uriString) {
        scrollState.scrollTo(0)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("win11_webapp_runtime_body")
    ) {
        // App Title & Live Launch Status Header
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = badgeColor.copy(alpha = 0.14f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, badgeColor.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = when {
                            report.dispatchedExternally -> Icons.Rounded.CheckCircle
                            isHtmlMistake -> Icons.Rounded.WarningAmber
                            else -> Icons.Rounded.Info
                        },
                        contentDescription = report.statusHeadline,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("webapp_runtime_title")
                    )
                    Text(
                        text = report.statusHeadline,
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("webapp_runtime_status")
                    )
                }
            }
        }

        // Clear Honest Explanation Card (So User Always Knows Exactly What Opened & Why)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = report.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("webapp_runtime_explanation")
                )
                Text(
                    text = "${report.extension.uppercase()} • ${formatFileSize(report.sizeBytes)} • ${report.headerInspection}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = Win11Sky
                )
            }
        }

        // Primary Actions Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { onRetryExternal(report) },
                colors = ButtonDefaults.buttonColors(containerColor = Win11Blue, contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .testTag("webapp_retry_external_button")
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.winWebAppRetryExternal,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(
                onClick = { onSearchRuntimeInChrome(runtimeSearchUrl) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("webapp_find_runtime_button")
            ) {
                Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.winWebAppFindRuntime,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        OutlinedButton(
            onClick = onOpenDownloadsFolder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.winOpenFolder, fontSize = 11.sp)
        }

        // Live Inspection & Execution Trace Console
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Terminal, contentDescription = null, tint = Win11Sky, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.winWebAppInspectLabel, style = MaterialTheme.typography.labelLarge)
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0A0D14),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                report.traceLines.forEach { line ->
                    Text(
                        text = line,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF93C5FD)
                    )
                }
            }
        }
    }
}
