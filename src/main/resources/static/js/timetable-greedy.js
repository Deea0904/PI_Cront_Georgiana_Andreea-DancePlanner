const daysMap = ["", "Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"];



function renderTimetable(data) {
    const container = document.getElementById("timetable-container");
    if (data.length === 0) {
        container.innerHTML = "<p>Niciun curs.</p>";
        return;
    }

    const grouped = {};
    data.forEach(e => {
        if (!grouped[e.dayOfWeek]) grouped[e.dayOfWeek] = [];
        grouped[e.dayOfWeek].push(e);
    });

    container.innerHTML = "";
    Object.keys(grouped).sort().forEach(day => {
        const card = document.createElement("div");
        card.className = "day-card";
        let html = `<div class="day-title">${daysMap[day]}</div>`;

        grouped[day].sort((a,b) => a.startTime.localeCompare(b.startTime)).forEach(s => {
            const isPriv = s.eventType === "PRIVATE";
            html += `
                <div class="session-row">
                    <div class="session-time">${s.startTime}</div>
                    <span class="badge ${isPriv ? 'badge-privat' : 'badge-grup'}">${isPriv ? 'PRIVAT' : 'GRUP'}</span>
                    <div class="session-info">
                        <div class="session-name">${isPriv ? s.dancerName : s.groupName}</div>
                        <div class="session-hall">Sala: ${s.hallName} | Coach: ${s.coachName}</div>
                    </div>
                </div>`;
        });
        card.innerHTML = html;
        container.appendChild(card);
    });
}