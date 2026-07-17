// Theme Toggle Script for CVLens
document.addEventListener("DOMContentLoaded", () => {
    // Check local storage for theme configuration
    const currentTheme = localStorage.getItem("theme") || "light";
    if (currentTheme === "dark") {
        document.body.classList.add("dark-theme");
        updateToggleIcons("dark");
    } else {
        updateToggleIcons("light");
    }

    // Bind event listener to the toggle button
    const themeToggleBtn = document.getElementById("theme-toggle");
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener("click", () => {
            document.body.classList.toggle("dark-theme");
            let theme = "light";
            if (document.body.classList.contains("dark-theme")) {
                theme = "dark";
            }
            localStorage.setItem("theme", theme);
            updateToggleIcons(theme);
        });
    }
});

// Update the toggle button icon
function updateToggleIcons(theme) {
    const themeIcon = document.getElementById("theme-icon");
    if (themeIcon) {
        if (theme === "dark") {
            themeIcon.className = "bi bi-sun-fill text-warning";
        } else {
            themeIcon.className = "bi bi-moon-fill";
        }
    }
}
