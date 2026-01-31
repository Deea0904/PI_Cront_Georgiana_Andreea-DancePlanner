function dayName(d) {
    return ["", "Luni", "Marți", "Miercuri", "Joi", "Vineri", "Sâmbătă", "Duminică"][d] || ("Zi " + d);
}

function setStatus(text) {
    const el = document.getElementById("statusMsg");
    if (el) el.textContent = text || "";
}

async function loadGroupsFilter() {
    const box = document.getElementById("groupsBox");
    if (!box) return;

    box.innerHTML = "";

    try {
        const res = await fetch("/api/groups");
        if (!res.ok) throw new Error("Eroare " + res.status + " la /api/groups");

        const groups = await res.json();
        if (!Array.isArray(groups) || groups.length === 0) {
            box.textContent = "Nu există grupe.";
            setStatus("Nu există grupe.");
            return;
        }

        groups.forEach(g => {
            const wrap = document.createElement("label");
            wrap.style.display = "inline-block";
            wrap.style.marginRight = "12px";
            wrap.style.marginBottom = "6px";

            const name = g.name ?? ("Grupa " + g.id);

            wrap.innerHTML = `
                <input type="checkbox" class="groupChk" value="${g.id}">
                ${name}
            `;
            box.appendChild(wrap);
        });

        setStatus("Selectează grupele dorite și apasă Reîncarcă.");

    } catch (e) {
        console.error(e);
        box.textContent = "Eroare la încărcarea grupelor.";
        setStatus("Eroare la încărcarea grupelor: " + e.message);
    }
}

function getSelectedGroupIds() {
    return Array.from(document.querySelectorAll(".groupChk:checked"))
        .map(chk => chk.value);
}

async function loadCalendar() {
    const table = document.getElementById("calendarTable");
    if (!table) return;

    const dayFilter = document.getElementById("dayFilter")?.value || "";
    const includeGroup = document.getElementById("showGroup")?.checked;
    const includePrivate = document.getElementById("showPrivate")?.checked;
    const groupIds = getSelectedGroupIds();

    setStatus("Se încarcă orarul...");

    if (!includeGroup && !includePrivate) {
        setStatus("Bifează Ore de grup și/sau Ore private.");
        renderCalendarTable([]);
        return;
    }

    // CONSTRUCTIE URL CORECTA: Trimitem mereu parametrii de tip (includeGroup/Private)
    const params = new URLSearchParams();
    params.set("includeGroup", String(includeGroup));
    params.set("includePrivate", String(includePrivate));

    if (groupIds.length > 0) {
        params.set("groupIds", groupIds.join(","));
    }

    const url = "/api/calendar?" + params.toString();

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error(await res.text() || "Eroare " + res.status);

        let data = await res.json();

        // Filtrare pe zi (client-side)
        if (dayFilter) {
            data = data.filter(e => String(e.dayOfWeek) === String(dayFilter));
        }

        renderCalendarTable(data);
        setStatus(`S-au încărcat ${data.length} sesiuni.`);
    } catch (e) {
        console.error(e);
        setStatus("Eroare: " + e.message);
        renderCalendarTable([]);
    }
}
function renderCalendarTable(data) {
    const table = document.getElementById("calendarTable");
    table.innerHTML = "";

    const thead = document.createElement("thead");
    const hr = document.createElement("tr");

    ["Zi", "Start", "End", "Tip", "Sală", "Coach", "Detalii"].forEach(h => {
        const th = document.createElement("th");
        th.textContent = h;
        hr.appendChild(th);
    });

    thead.appendChild(hr);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");

    if (!Array.isArray(data) || data.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 7;
        td.textContent = "Nu există sesiuni pentru filtrele selectate.";
        tr.appendChild(td);
        tbody.appendChild(tr);
        table.appendChild(tbody);
        return;
    }

    data.forEach(e => {
        const tr = document.createElement("tr");

        const details =
            e.eventType === "GROUP"
                ? (e.groupName ? ("Grupa: " + e.groupName) : ("Grupa #" + (e.groupLevelId ?? "?")))
                : (e.dancerName ? ("Dansator: " + e.dancerName) : ("Dansator #" + (e.dancerId ?? "?")));


        tr.innerHTML = `
            <td>${dayName(e.dayOfWeek)}</td>
            <td>${e.startTime}</td>
            <td>${e.endTime}</td>
            <td>${e.eventType}</td>
            <td>${e.hallName ?? ("#" + e.hallId)}</td>
            <td>${e.coachName ?? ("#" + e.coachId)}</td>
            <td>${details}</td>

        `;

        tbody.appendChild(tr);
    });

    table.appendChild(tbody);
}

document.addEventListener("DOMContentLoaded", async () => {
    await loadGroupsFilter();
    await loadCalendar();

    document.getElementById("reloadBtn")?.addEventListener("click", loadCalendar);
    document.getElementById("dayFilter")?.addEventListener("change", loadCalendar);
    document.getElementById("showGroup")?.addEventListener("change", loadCalendar);
    document.getElementById("showPrivate")?.addEventListener("change", loadCalendar);

    // cand bifezi/debifezi grupe -> reload automat
    document.addEventListener("change", (e) => {
        if (e.target && e.target.classList && e.target.classList.contains("groupChk")) {
            loadCalendar();
        }
    });
});
