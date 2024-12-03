import sys
import subprocess
from aqt import mw
from aqt.qt import QAction, QIcon
from aqt.utils import showInfo, showWarning
from PyQt6.QtCore import QCoreApplication

def restart_anki():
    if sys.platform != "darwin":
        showWarning("This add-on is only available on macOS. It will not function on your platform.")
        return

    # Show a confirmation dialog before restarting
    showInfo("Anki is restarting...")

    # Now, spawn a new process to launch Anki after sleeping for 1 second
    if sys.platform == "darwin":
        # macOS: Use `sleep` inside the subprocess followed by `open` to run Anki
        subprocess.Popen("sleep 1 && open -a Anki", shell=True)

    # Quit Anki after starting the subprocess to avoid terminating it
    QCoreApplication.quit()

# Create the action to trigger the restart
def add_restart_action():
    action = QAction(QIcon(), "Restart Anki", mw)
    action.triggered.connect(restart_anki)
    mw.form.menuTools.addAction(action)

# Add the restart action to Anki's Tools menu when the add-on is loaded
add_restart_action()
