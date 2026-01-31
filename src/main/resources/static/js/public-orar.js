const daysMap = ["", "Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"];

async function loadPublicTimetable() {
    const container = document.getElementById("timetable-container");
    if (!container) return;

    try {
        const response = await fetch('/api/calendar');
        const data = await response.json();

        if (data.length === 0) {
            container.innerHTML = "<p>Momentan nu există cursuri programate.</p>";
            return;
        }

        const grouped = {};
        data.forEach(event => {
            if (!grouped[event.dayOfWeek]) grouped[event.dayOfWeek] = [];
            grouped[event.dayOfWeek].push(event);
        });

        container.innerHTML = "";

        Object.keys(grouped).sort().forEach(dayNum => {
            const dayCard = document.createElement("div");
            dayCard.className = "day-card";

            let sessionsHtml = `<div class="day-title">${daysMap[dayNum]}</div>`;

            grouped[dayNum].sort((a,b) => a.startTime.localeCompare(b.startTime)).forEach(session => {
                const isPriv = session.eventType === "PRIVATE";
                const typeClass = isPriv ? "badge-privat" : "badge-grup";
                const typeLabel = isPriv ? "PRIVAT" : "GRUP";
                const displayName = isPriv ? session.dancerName : session.groupName;

                sessionsHtml += `
                    <div class="session-row">
                        <div class="session-time">${session.startTime}</div>
                        <span class="badge ${typeClass}">${typeLabel}</span>
                        <div class="session-info">
                            <div class="session-name">${displayName || 'Curs'}</div>
                            <div class="session-hall">Sala: ${session.hallName} | Coach: ${session.coachName}</div>
                        </div>
                    </div>`;
            });

            dayCard.innerHTML = sessionsHtml;
            container.appendChild(dayCard);
        });
    } catch (err) {
        container.innerHTML = "<p>Eroare la încărcarea orarului.</p>";
    }
}

document.addEventListener("DOMContentLoaded", loadPublicTimetable);