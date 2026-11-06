package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampError
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampsiteAppTheme
import java.util.Date
import kotlin.compareTo

@Composable
fun CampsiteScreen(
    repository: CampsiteRepo,
    onAddCampsite: (Campsite) -> Unit,
    onRemoveCampsite: (Campsite) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newCampsiteName by remember { mutableStateOf("") }
    var newCampsiteId by remember { mutableStateOf("") }
    var showAddCampsiteFields by remember { mutableStateOf(false) }
    var showRemoveCampsiteFields by remember { mutableStateOf(false) }
    var searchBarText by remember { mutableStateOf("") }
    val campsites = repository.campsites
    repository.searchQuery = searchBarText


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CampBackground)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CampGreen)
                .statusBarsPadding()
                .height(56.dp)
        ) {
            TextButton(
                onClick = { onBack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("backButton")
            ) {
                Text(text = "←", color = Color.White, fontSize = 22.sp)
            }

            Text(
                text = "Campsites",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = searchBarText,
            onValueChange = {
                searchBarText = it
            },
            label = { Text("Search") },
            colors = fieldColors(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("searchBar")
                .padding(horizontal = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("addFieldButton"),
                onClick = {
                    showAddCampsiteFields = !showAddCampsiteFields
                    showRemoveCampsiteFields = false
                },
                containerColor = CampGreen,
                contentColor = Color.White
            ) {
                Text("+")
            }
            FloatingActionButton(
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("removeFieldButton"),
                onClick = {
                    showRemoveCampsiteFields = !showAddCampsiteFields
                    showAddCampsiteFields = false
                    if (showRemoveCampsiteFields) {
                        newCampsiteName = ""
                        newCampsiteId = ""
                    }
                },
                containerColor = Color.Red,
                contentColor = Color.White
            ) {
                Text("-")
            }
        }
        if (showAddCampsiteFields) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TextField(
                    value = newCampsiteName,
                    onValueChange = { newCampsiteName = it },
                    label = { Text("Campsite") },
                    colors = fieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nameInput")
                )

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = newCampsiteId,
                    onValueChange = { newCampsiteId = it },
                    label = { Text("ID") },
                    colors = fieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("idInput")
                )
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .testTag("addCampsiteButton"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CampGreen,
                        contentColor = Color.White
                    ),
                    onClick = {
                        if (newCampsiteName.isNotBlank() && newCampsiteId.isNotBlank()) {
                            onAddCampsite(
                                Campsite(
                                    id = newCampsiteId,
                                    name = newCampsiteName
                                )
                            )

                            newCampsiteName = ""
                            newCampsiteId = ""
                            showAddCampsiteFields = false
                        }
                    }
                ) {
                    Text("ADD CAMPSITE")
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(campsites) { index, campsite ->
                CampsiteRow(
                    campsite = campsite,
                    onClick = {
                        if (showRemoveCampsiteFields) {
                            onRemoveCampsite(campsite)
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampsiteScreenPreview() {
    val repo = CampsiteRepositoryStud()
    repo.addCampsite(Campsite(name = "Camp Melody", id = "1"))
    repo.addCampsite(Campsite(name = "Bingus", id = "2"))
    repo.addCampsite(Campsite(name = "Balls", id = "3"))
    CampsiteAppTheme {
        CampsiteScreen(
            repository = repo,
            onAddCampsite = { _ ->},
            onRemoveCampsite = { _ -> },
            onBack = {}
        )
    }
}

class CampsiteRepositoryStud : CampsiteRepo {
    private val _campsites = mutableStateListOf<Campsite>()

    override var searchQuery: String by mutableStateOf("")
    override var minGuests: Int by mutableIntStateOf(0)
    override var selectedLocation: String by mutableStateOf("")
    override var selectedDate: Date by mutableStateOf(Date(0L))

    override val campsites: List<Campsite>
        get() = _campsites

    override fun addCampsite(campsite: Campsite) {
        _campsites.add(campsite)
    }

    override fun removeCampsite(campsite: Campsite) {
        _campsites.remove(campsite)
    }

    override fun getCampsitesByOwner(ownerId: String): List<Campsite> {
        return _campsites.filter { it.ownerId == ownerId }
    }
}