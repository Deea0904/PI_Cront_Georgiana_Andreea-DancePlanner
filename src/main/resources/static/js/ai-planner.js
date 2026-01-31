const daysMap = { 1: "Luni", 2: "Marti", 3: "Miercuri", 4: "Joi", 5: "Vineri", 6: "Sambata", 7: "Duminica" };
window.currentSuggestions = [];

document.getElementById("generateWeeklyBtn").addEventListener("click", () => {
    fetchSuggestions("/api/ai/schedule/generate-weekly");
});

document.getElementById("approveAllBtn").addEventListener("click", approveAll);

async function fetchSuggestions(url) {
    const loader = document.getElementById("loadingMsg");
    const table = document.getElementById("resultsTable");
    const tbody = document.getElementById("resultsBody");
    const approveAllBtn = document.getElementById("approveAllBtn");

    loader.style.display = "block";
    table.style.display = "none";
    approveAllBtn.style.display = "none";

    try {
        const response = await fetch(url);
        window.currentSuggestions = await response.json();
        renderTable();

        if (window.currentSuggestions.length > 0) {
            approveAllBtn.style.display = "block";
            document.getElementById("generationSummary").innerText = `S-au generat ${window.currentSuggestions.length} sugestii de lectii private.`;
        }
    } catch (err) {
        alert("Eroare la generare AI.");
    } finally { loader.style.display = "none"; }
}

function renderTable() {
    const tbody = document.getElementById("resultsBody");
    const table = document.getElementById("resultsTable");
    tbody.innerHTML = "";

    window.currentSuggestions.sort((a, b) => a.dayOfWeek - b.dayOfWeek || a.startTime.localeCompare(b.startTime));

    window.currentSuggestions.forEach((entry, index) => {
        const row = `<tr>
            <td><strong>${daysMap[entry.dayOfWeek]}</strong></td>
            <td>${entry.startTime} - ${entry.endTime}</td>
            <td>Sala ${entry.hallId}</td>
            <td>${entry.coachName || "Coach " + entry.coachId}</td>
            <td>${entry.dancerName || "Dansator " + entry.dancerId}</td>
            <td><button style="background:#d9534f; color:white; border:none; padding:5px 10px; cursor:pointer;" 
                onclick="removeSuggestion(${index})">Sterge</button></td>
        </tr>`;
        tbody.innerHTML += row;
    });
    table.style.display = window.currentSuggestions.length > 0 ? "table" : "none";
}

function removeSuggestion(index) {
    window.currentSuggestions.splice(index, 1);
    renderTable();
    document.getElementById("generationSummary").innerText = `S-au generat ${window.currentSuggestions.length} sugestii.`;
}

async function approveAll() {
    if (!confirm("Sigur doresti sa stergi calendarul curent si sa salvezi acest orar nou (Grup + Privat)?")) return;

    try {
        const response = await fetch('/api/admin/calendar/approve-full-schedule', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(window.currentSuggestions)
        });

        if (response.ok) {
            alert("Orarul a fost salvat cu succes in calendar_event!");
            window.location.href = "admin-timetable.html"; // Redirect inapoi la vizualizare
        } else {
            const err = await response.text();
            alert("Eroare la salvare: " + err);
        }
    } catch (err) { alert("Eroare de retea."); }
}