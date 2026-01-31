// ===== PAGINA ANTRENOR (final) =====

// ---------- Asistenta UI ----------
function showPrivateSections() {
    // ascunde formularul
    document.getElementById("loginBox")?.classList.add("hidden");

    // arata zona dupa login (daca exista)
    document.getElementById("coachContent")?.classList.remove("hidden");

    // arata rubricile private (daca exista)
    document.getElementById("privateSections")?.classList.remove("hidden");

    // buton conectare -> conectat
    const toggleLoginBtn = document.getElementById("toggleLoginBtn");
    if (toggleLoginBtn) {
        toggleLoginBtn.textContent = "Conectat";
        toggleLoginBtn.disabled = true;
    }

    // arata logout (daca exista)
    document.getElementById("logoutBtn")?.classList.remove("hidden");
}

function setLoginMsg(text, type = "") {
    const el = document.getElementById("loginMsg");
    if (!el) return;
    el.textContent = text;
    el.classList.remove("ok", "err");
    if (type) el.classList.add(type);
}

// ---------- Sesiune ----------
async function checkSession() {
    try {
        const res = await fetch("/api/coach/me", { credentials: "include" });
        if (!res.ok) return;

        const usernameFromSession = await res.text();
        const nameEl = document.getElementById("coachName");
        if (nameEl) nameEl.textContent = usernameFromSession;

        showPrivateSections();
        await onCoachLoggedInExtras();
    } catch (e) {
        console.error("checkSession error:", e);
    }
}

// ---------- Tabele ----------
function renderTable(tableId, data) {
    const table = document.getElementById(tableId);
    if (!table) return;

    table.innerHTML = "";

    if (!data || !Array.isArray(data) || data.length === 0) {
        table.innerHTML = "<tr><td>Nimic de afișat.</td></tr>";
        return;
    }

    const keysSet = new Set();
    data.forEach((obj) => Object.keys(obj).forEach((k) => keysSet.add(k)));
    const allKeys = Array.from(keysSet);

    const preferredOrder = ["id", "name", "description"];
    const orderedKeys = [];
    preferredOrder.forEach((k) => {
        if (allKeys.includes(k)) orderedKeys.push(k);
    });
    allKeys.forEach((k) => {
        if (!orderedKeys.includes(k)) orderedKeys.push(k);
    });

    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");
    orderedKeys.forEach((k) => {
        const th = document.createElement("th");
        th.textContent = k;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");
    data.forEach((obj) => {
        const tr = document.createElement("tr");
        orderedKeys.forEach((k) => {
            const td = document.createElement("td");
            const val = obj[k];
            td.textContent =
                val !== null && typeof val === "object"
                    ? JSON.stringify(val)
                    : val !== undefined
                        ? val
                        : "";
            tr.appendChild(td);
        });
        tbody.appendChild(tr);
    });
    table.appendChild(tbody);
}

async function loadCoaches() {
    const statusEl = document.getElementById("coach-status");
    if (statusEl) statusEl.textContent = "Se încarcă antrenorii...";

    try {
        const resp = await fetch("/api/coaches");
        if (!resp.ok) throw new Error("Eroare " + resp.status + " la /api/coaches");

        const data = await resp.json();
        renderTable("coach-table", data);

        if (statusEl)
            statusEl.textContent =
                "S-au încărcat " + (Array.isArray(data) ? data.length : 0) + " antrenori.";
    } catch (e) {
        console.error(e);
        if (statusEl) statusEl.textContent = "Eroare la încărcare: " + e.message;
    }
}

async function loadGroupsIntoTable() {
    const statusEl = document.getElementById("groups-status");
    if (statusEl) statusEl.textContent = "Se încarcă grupele...";

    try {
        const resp = await fetch("/api/groups");
        if (!resp.ok) throw new Error("Eroare " + resp.status + " la /api/groups");
        const data = await resp.json();
        renderTable("groups-table", data);

        if (statusEl)
            statusEl.textContent =
                "S-au încărcat " + (Array.isArray(data) ? data.length : 0) + " grupe.";
    } catch (e) {
        console.error(e);
        if (statusEl) statusEl.textContent = "Eroare la încărcare: " + e.message;
    }
}

// ---------- Actiuni Autentificare ----------
async function loginCoach() {
    const username = document.getElementById("username")?.value?.trim() || "";
    const password = document.getElementById("password")?.value || "";

    const res = await fetch("/api/coach/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
        credentials: "include",
    });

    if (res.ok) {
        const meRes = await fetch("/api/coach/me", { credentials: "include" });
        if (meRes.ok) {
            const usernameFromSession = await meRes.text();
            const nameEl = document.getElementById("coachName");
            if (nameEl) nameEl.textContent = usernameFromSession;
        }
        showPrivateSections();

        await onCoachLoggedInExtras();
    } else {
        const msg = await res.text();
        setLoginMsg(msg || "Eroare " + res.status, "err");
    }
}

async function logoutCoach() {
    try {
        await fetch("/api/coach/logout", {
            method: "POST",
            credentials: "include",
        });
        window.location.reload();
    } catch (e) {
        console.error("Logout error:", e);
    }
}

async function changeCoachPassword() {
    const oldPassword = document.getElementById("oldPass")?.value || "";
    const newPassword = document.getElementById("newPass")?.value || "";
    const confirmPassword = document.getElementById("confirmNewPass")?.value || "";

    const msg = document.getElementById("changePassMsg");
    if (!msg) return;

    // validare frontend
    if (!newPassword || !confirmPassword) {
        msg.textContent = "Completeaza ambele campuri pentru parola noua.";
        msg.className = "err";
        return;
    }
    if (newPassword !== confirmPassword) {
        msg.textContent = "Parolele noi nu coincid.";
        msg.className = "err";
        return;
    }
    if (newPassword.length < 6) {
        msg.textContent = "Parola trebuie sa aiba minim 6 caractere.";
        msg.className = "err";
        return;
    }

    const res = await fetch("/api/coach/change-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ oldPassword, newPassword }),
        credentials: "include",
    });

    if (res.ok) {
        msg.textContent = "Parola a fost schimbata cu succes.";
        msg.className = "ok";

        document.getElementById("oldPass").value = "";
        document.getElementById("newPass").value = "";
        document.getElementById("confirmNewPass").value = "";
    } else {
        msg.textContent = "Parola veche este gresita.";
        msg.className = "err";
    }
}

// ---------- INIT ----------
document.addEventListener("DOMContentLoaded", () => {
    // verifica sesiunea (sa nu delogheze la refresh)
    checkSession();

    // comutare formular login
    const toggleLoginBtn = document.getElementById("toggleLoginBtn");
    const loginBox = document.getElementById("loginBox");
    if (toggleLoginBtn && loginBox) {
        toggleLoginBtn.addEventListener("click", () => {
            loginBox.classList.toggle("hidden");
            setLoginMsg("");
        });
    }

    // trimitere login
    document.getElementById("loginBtn")?.addEventListener("click", loginCoach);

    // deconectare
    document.getElementById("logoutBtn")?.addEventListener("click", logoutCoach);

    // comutare schimbare parola (optional)
    const toggleText = document.getElementById("toggleChangePass");
    const box = document.getElementById("changePasswordBox");
    if (toggleText && box) {
        toggleText.addEventListener("click", () => {
            box.classList.toggle("hidden");
        });
    }

    // trimitere schimbare parola
    document.getElementById("changePassBtn")?.addEventListener("click", changeCoachPassword);

    // comutare tabel antrenori
    const btn = document.getElementById("toggleViewCoachesBtn");
    const tableWrap = document.getElementById("coach-table"); // wrapper-ul care contine tabelul
    if (btn && tableWrap) {
        btn.addEventListener("click", async () => {
            if (tableWrap.classList.contains("hidden")) {
                await loadCoaches();
                tableWrap.classList.remove("hidden");
                btn.textContent = "Ascunde antrenorii";
            } else {
                tableWrap.classList.add("hidden");
                btn.textContent = "Încarcă antrenorii";
            }
        });
    }

    // comutare tabel grupe
    const btnGroup = document.getElementById("toggleViewGroupsBtn");
    const tableGroups = document.getElementById("groups-table"); // wrapper-ul tau pt tabel
    if (btnGroup && tableGroups) {
        btnGroup.addEventListener("click", async () => {
            const isHidden = tableGroups.classList.contains("hidden");
            if (isHidden) {
                await loadGroupsIntoTable();
                tableGroups.classList.remove("hidden");
                btnGroup.textContent = "Ascunde grupele";
            } else {
                tableGroups.classList.add("hidden");
                btnGroup.textContent = "Încarcă grupele din /api/groups";
            }
        });
    }
});

// ================== DISPONIBILITATE ANTRENOR ==================

function setAvailMsg(text, type = "") {
    const el = document.getElementById("availMsg");
    if (!el) return;
    el.textContent = text || "";
    el.classList.remove("ok", "err");
    if (type) el.classList.add(type);
}

function dayName(d) {
    return ["", "Luni", "Marti", "Miercuri", "Joi", "Vineri", "Sambata", "Duminica"][d] || ("Zi " + d);
}

async function loadCoachAvailability() {
    const table = document.getElementById("availTable");
    if (!table) return;

    const res = await fetch("/api/coach/availability", { credentials: "include" });
    if (!res.ok) {
        const txt = await res.text();
        table.innerHTML = "";
        return setAvailMsg(txt || ("Eroare availability: " + res.status), "err");
    }

    const data = await res.json();
    renderCoachAvailabilityTable(data);
}

function renderCoachAvailabilityTable(data) {
    const table = document.getElementById("availTable");
    table.innerHTML = "";

    const thead = document.createElement("thead");
    const hr = document.createElement("tr");
    ["id", "zi", "start", "end", "actiuni"].forEach(h => {
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
        td.colSpan = 5;
        td.textContent = "Nu ai intervale salvate.";
        tr.appendChild(td);
        tbody.appendChild(tr);
        table.appendChild(tbody);
        return;
    }

    data.forEach(row => {
        const tr = document.createElement("tr");

        [row.id, dayName(row.dayOfWeek), row.startTime, row.endTime].forEach(v => {
            const td = document.createElement("td");
            td.textContent = v;
            tr.appendChild(td);
        });

        const tdA = document.createElement("td");
        const btn = document.createElement("button");
        btn.textContent = "Sterge";
        btn.onclick = async () => {
            await deleteCoachAvailability(row.id);
            await loadCoachAvailability();
        };
        tdA.appendChild(btn);
        tr.appendChild(tdA);

        tbody.appendChild(tr);
    });

    table.appendChild(tbody);
}

async function addCoachAvailability() {
    const dayOfWeek = parseInt(document.getElementById("availDay").value, 10);
    const startTime = document.getElementById("availStart").value;
    const endTime = document.getElementById("availEnd").value;

    if (!startTime || !endTime) return setAvailMsg("Completeaza start/end.", "err");
    if (startTime >= endTime) return setAvailMsg("Start trebuie < End.", "err");

    const res = await fetch("/api/coach/availability", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ dayOfWeek, startTime, endTime })
    });

    const txt = await res.text();
    if (!res.ok) return setAvailMsg(txt || ("Eroare add availability: " + res.status), "err");

    setAvailMsg("Disponibilitate salvata.", "ok");
}

async function deleteCoachAvailability(id) {
    const res = await fetch("/api/coach/availability/" + id, {
        method: "DELETE",
        credentials: "include"
    });

    if (!(res.ok || res.status === 204)) {
        const txt = await res.text();
        return setAvailMsg(txt || ("Eroare stergere: " + res.status), "err");
    }

    setAvailMsg("Interval sters.", "ok");
}

function initCoachAvailabilityUI() {
    document.getElementById("addAvailBtn")?.addEventListener("click", async () => {
        await addCoachAvailability();
        await loadCoachAvailability();
    });
}

async function onCoachLoggedInExtras() {
    initCoachAvailabilityUI();
    await loadCoachAvailability();
}


