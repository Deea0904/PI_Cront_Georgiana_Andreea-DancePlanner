
document.getElementById("logoutBtn")?.addEventListener("click", (e) => {
    window.location.href = "/logout";
});
const daysMap = ["", "Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"];

async function generateGreedySchedule() {
    console.log("Buton apăsat! Se trimite cererea către server..."); // DEBUG
    const container = document.getElementById("timetable-container");
    container.innerHTML = "<div class='loading-spinner'>⚡ Calculăm cel mai bun orar disponibil...</div>";

    try {
        const response = await fetch('/api/schedule/greedy/generate');
        console.log("Răspuns primit de la server:", response.status); // DEBUG

        if (!response.ok) throw new Error("Eroare la server: " + response.status);

        const data = await response.json();
        console.log("Date primite:", data); // DEBUG
        renderGreedyTimetable(data);
    } catch (err) {
        console.error("Eroare:", err);
        container.innerHTML = "<div style='color: red; padding: 20px;'>❌ Eroare: " + err.message + "</div>";
    }
}

function renderGreedyTimetable(data) {
    const container = document.getElementById("timetable-container");
    if (!data || data.length === 0) {
        container.innerHTML = "<p>Nu s-au putut genera lecții private. Verificați disponibilitățile.</p>";
        return;
    }

    const grouped = {};
    data.forEach(event => {
        if (!grouped[event.dayOfWeek]) grouped[event.dayOfWeek] = [];
        grouped[event.dayOfWeek].push(event);
    });

    let html = "<h3 style='width: 100%; margin-top: 30px; grid-column: 1/-1;'>Preview Orar Privat Generat</h3>";

    Object.keys(grouped).sort().forEach(dayNum => {
        html += `<div class="day-card" style="background: #f9f9f9; border-left: 5px solid #27ae60; padding: 15px; margin: 10px; border-radius: 4px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); flex: 1; min-width: 280px;">
            <strong style="font-size: 1.1rem;">${daysMap[dayNum]}</strong><hr style="border: 0; border-top: 1px solid #ddd; margin: 10px 0;">`;

        grouped[dayNum].sort((a,b) => a.startTime.localeCompare(b.startTime)).forEach(s => {
            html += `<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; padding: 5px; background: white; border-radius: 4px;">
                <div>
                    <span style="color: #e67e22; font-weight: bold;">${s.startTime} - ${s.endTime}</span><br>
                    <strong>${s.dancerName}</strong><br>
                    <small style="color: #666;">Coach: ${s.coachName} | <i>${s.hallName}</i></small>
                </div>
            </div>`;
        });
        html += `</div>`;
    });

    container.innerHTML = `<div style="display: flex; flex-wrap: wrap; width: 100%;">${html}</div>`;
}