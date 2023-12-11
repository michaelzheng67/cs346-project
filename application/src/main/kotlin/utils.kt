import androidx.compose.ui.awt.ComposeWindow
import net.codebot.models.Pin
import java.awt.FileDialog
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun validateDate(date: String): Boolean {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        formatter.isLenient = false
        var formatted = formatter.parse(date)
        true
    } catch (e: ParseException) {
        false
    }
}

fun validateTrip(
    name: String,
    description: String,
    startTime: String,
    endTime: String,
    status: String,
    pins: List<Pin>
): MutableList<String> {

    val messages = mutableListOf<String>()

    if (name == "") {
        messages.add("Invalid name.")
    }

    if (description == "") {
        messages.add("Invalid trip description.")
    }

    if (!validateDate(startTime) or !validateDate(endTime)) {
        messages.add("Invalid start date")
    } else if (isLaterThan(startTime, endTime)) {
        messages.add("You can't time travel! (start time must be before end time)")
    }

    if (!validateDate(endTime)) {
        messages.add("Invalid end date.")
    }

    val options = listOf("Planned", "In Progress", "Completed")
    if (status !in options) {
        messages.add("Invalid status.")
    }

    pins.forEachIndexed { i, pin ->
        if (pin.city == "") {
            messages.add("Invalid city at stop ${i + 1}")
        }
        if (pin.location == "") {
            messages.add("Invalid location at stop ${i + 1}")
        }
        if (pin.description == "") {
            messages.add("Invalid description at stop ${i + 1}")
        }
    }

    return messages
}

/*Assumes dd/mm/yyyy formatting
Returns true if date1 is later than date2
 */
fun isLaterThan(date1: String, date2: String): Boolean {
    // Define the date format
    val dateFormat = SimpleDateFormat("dd/MM/yyyy")

    try {
        // Parse the date strings into Date objects
        val parsedDate1: Date = dateFormat.parse(date1)
        val parsedDate2: Date = dateFormat.parse(date2)

        // Compare the Date objects to determine which date is later
        return parsedDate1.after(parsedDate2)
    } catch (e: Exception) {
        // Handle parsing exceptions, e.g., invalid date format
        e.printStackTrace()
        return false
    }
}

// Using following resources:
// https://www.reddit.com/r/Kotlin/comments/n16u8z/desktop_compose_file_picker/
// https://stackoverflow.com/questions/12558413/how-to-filter-file-type-in-filedialog
// https://stackoverflow.com/questions/37066216/java-encode-file-to-base64-string-to-match-with-other-encoded-string
fun uploadImage(
    window: ComposeWindow,
    title: String,
    allowMultiSelection: Boolean = false
): String {
    val fd = FileDialog(window, title, FileDialog.LOAD).apply {
        isMultipleMode = allowMultiSelection
        isVisible = true
    }
    fd.setFile("*.jpg;*.jpeg;*.png")
    val image = fd.files.toList()
    if (image.size == 1) {
        val fileContent = image[0].readBytes()
        return Base64.getEncoder().encodeToString(fileContent)
    } else {
        return ""
    }
}