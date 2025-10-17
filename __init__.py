import subprocess
import sys

from aqt import mw
from aqt.qt import QAction, QIcon, QMessageBox
from aqt.utils import showWarning
from PyQt6.QtCore import QCoreApplication


def restart_anki():
    if sys.platform != "darwin":
        showWarning(
            "This add-on is only available on macOS. "
            "It will not function on your platform."
        )
        return

    # Get config
    config = mw.addonManager.getConfig(__name__) or {}
    show_confirmation = config.get("show_confirmation_dialog", True)

    # Show confirmation dialog if enabled
    if show_confirmation:
        reply = QMessageBox.question(
            mw,
            "Restart Anki",
            "Restart Anki?",
            QMessageBox.StandardButton.Yes | QMessageBox.StandardButton.No,
            QMessageBox.StandardButton.No,
        )
        if reply != QMessageBox.StandardButton.Yes:
            return

    # Spawn a new process to launch Anki after sleeping for 1 second
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
