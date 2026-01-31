function setMsg(text, type = "") {
    const el = document.getElementById("msg");
    if (el) { el.textContent = text || ""; el.className = "msg " + type; }
}

function dayName(d) {
    const days = ["", "Luni", "Marti", "Miercuri", "Joi", "Vineri", "Sambata", "Duminica"];
    return days[d] || ("Zi " + d);
}

let mapsCache = null;

async function loadDropdowns() {
    try {
        const [halls, coaches, groups, dancers] = await Promise.all([
            fetch("/api/halls").then(r => r.json()),
            fetch("/api/coaches").then(r => r.json()),
            fetch("/api/groups").then(r => r.json()),
            fetch("/api/dancers").then(r => r.json())
        ]);

        const maps = {
            halls: new Map(halls.map(h => [h.id, h.name || "Sala " + h.id])),
            coaches: new Map(coaches.map(c => [c.id, c.name])),
            groups: new Map(groups.map(g => [g.id, g.name])),
            dancers: new Map(dancers.map(d => [d.id, d.name || d.username || "Dancer " + d.id]))
        };

        const populate = (id, list) => {
            const sel = document.getElementById(id);
            if (sel) sel.innerHTML = list.map(i => `<option value="${i.id}">${i.id}. ${i.name || i.username || 'N/A'}</option>`).join('');
        };

        populate("hallSelect", halls);
        populate("coachSelect", coaches);
        populate("groupSelect", groups);

        return maps;
    } catch (e) { console.error("Eroare la incarcare:", e); return null; }
}

function renderTable(tableId, data, maps, isGroup) {
    const table = document.getElementById(tableId);
    if (!table) return;
    table.innerHTML = "";
    if (!data || data.length === 0) {
        table.innerHTML = "<tr><td colspan='8'>Nu exista date programate.</td></tr>";
        return;
    }

    const headers = isGroup ? ["ID", "Zi", "Start", "End", "Sala", "Coach", "Grupa", "Actiuni"]
        : ["ID", "Zi", "Start", "End", "Sala", "Coach", "Dansator", "Actiuni"];

    let html = "<thead><tr>" + headers.map(h => `<th>${h}</th>`).join('') + "</tr></thead><tbody>";
    data.forEach(row => {
        const details = isGroup ? (maps.groups.get(row.groupLevelId) || row.groupLevelId)
            : (maps.dancers.get(row.dancerId) || row.dancerId);
        html += `<tr>
            <td>${row.id}</td><td>${dayName(row.dayOfWeek)}</td><td>${row.startTime}</td><td>${row.endTime}</td>
            <td>${maps.halls.get(row.hallId) || row.hallId}</td><td>${maps.coaches.get(row.coachId) || row.coachId}</td>
            <td>${details}</td>
            <td><button onclick="deleteEntry(${row.id}, ${isGroup})" style="background:#cc0000; color:white; border:none; padding:4px 8px; border-radius:4px; cursor:pointer;">Sterge</button></td>
        </tr>`;
    });
    table.innerHTML = html + "</tbody>";
}

async function loadAll() {
    if (!mapsCache) mapsCache = await loadDropdowns();
    try {
        const [groupRes, privateRes] = await Promise.all([
            fetch("/api/admin/timetable", { credentials: "include" }),
            fetch("/api/admin/private-timetable", { credentials: "include" })
        ]);
        if (groupRes.ok) renderTable("ttTable", await groupRes.json(), mapsCache, true);
        if (privateRes.ok) renderTable("prTable", await privateRes.json(), mapsCache, false);
    } catch (e) { console.error(e); }
}

async function deleteEntry(id, isGroup) {
    if (!confirm("Confirmati stergerea?")) return;
    const url = isGroup ? "/api/admin/timetable/" : "/api/admin/private-timetable/";
    const res = await fetch(url + id, { method: "DELETE", credentials: "include" });
    if (res.ok) loadAll();
}

document.addEventListener("DOMContentLoaded", async () => {
    await loadAll();
    document.getElementById("addEntryBtn")?.addEventListener("click", async () => {
        const payload = {
            dayOfWeek: parseInt(document.getElementById("daySelect").value),
            startTime: document.getElementById("startTime").value,
            endTime: document.getElementById("endTime").value,
            hallId: parseInt(document.getElementById("hallSelect").value),
            coachId: parseInt(document.getElementById("coachSelect").value),
            groupLevelId: parseInt(document.getElementById("groupSelect").value)
        };
        const res = await fetch("/api/admin/timetable", {
            method: "POST", headers: { "Content-Type": "application/json" },
            credentials: "include", body: JSON.stringify(payload)
        });
        if (res.ok) { setMsg("Sesiune grup adaugata.", "ok"); loadAll(); }
        else { setMsg("Eroare: " + await res.text(), "err"); }
    });
    document.getElementById("refreshBtn")?.addEventListener("click", loadAll);
});