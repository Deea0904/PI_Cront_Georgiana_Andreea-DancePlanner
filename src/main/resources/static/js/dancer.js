// js/dancer-page.js
function showPrivateSections() {
    document.getElementById("loginBox")?.classList.add("hidden");


    // arata zona dupa login (daca exista)
    document.getElementById("dancerContent")?.classList.remove("hidden");

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
//verifica daca e logat ca sa nu delogheze la refresh
async function checkSession() {
    try {
        const res = await fetch("/api/dancer/me", { credentials: "include" });
        if (res.ok) {
            const meRes = await fetch("/api/dancer/me", { credentials: "include" });
            if (meRes.ok) {
                const usernameFromSession = await meRes.text(); // string
                document.getElementById("dancerName").textContent = usernameFromSession;
            }
            showPrivateSections();

            await onDancerLoggedInExtras();
        }
    } catch (e) {
        console.error("checkSession error:", e);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    checkSession();
});


function renderTable(tableId, data) {
    const table = document.getElementById(tableId);
    if (!table) return;

    table.innerHTML = '';

    if (!data || !Array.isArray(data) || data.length === 0) {
        table.innerHTML = '<tr><td>Nimic de afișat.</td></tr>';
        return;
    }

    const keysSet = new Set();
    data.forEach(obj => Object.keys(obj).forEach(k => keysSet.add(k)));
    const allKeys = Array.from(keysSet);

    const preferredOrder = ['id', 'name', 'description'];
    const orderedKeys = [];
    preferredOrder.forEach(k => { if (allKeys.includes(k)) orderedKeys.push(k); });
    allKeys.forEach(k => { if (!orderedKeys.includes(k)) orderedKeys.push(k); });

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    orderedKeys.forEach(k => {
        const th = document.createElement('th');
        th.textContent = k;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    data.forEach(obj => {
        const tr = document.createElement('tr');
        orderedKeys.forEach(k => {
            const td = document.createElement('td');
            const val = obj[k];
            td.textContent =
                val !== null && typeof val === 'object'
                    ? JSON.stringify(val)
                    : (val !== undefined ? val : '');
            tr.appendChild(td);
        });
        tbody.appendChild(tr);
    });
    table.appendChild(tbody);
}

// ===== API DANSATORI (o singura data) =====
async function loadDancers() {
    const statusEl = document.getElementById('dancers-status');
    if (statusEl) statusEl.textContent = 'Se încarcă dancerii...';

    const resp = await fetch('/api/dancers');
    if (!resp.ok) throw new Error('Eroare ' + resp.status + ' la /api/dancers');

    const data = await resp.json();
    const hidden = new Set(["levelId"]);
    const filtered = Array.isArray(data)
        ? data.map(d => Object.fromEntries(Object.entries(d).filter(([k]) => !hidden.has(k))))
        : data;

    renderTable('dancers-table', filtered);

    if (statusEl) statusEl.textContent = 'S-au încărcat ' + (Array.isArray(data) ? data.length : 0) + ' dansatori.';
}

async function loadGroupsIntoTable() {
    const statusEl = document.getElementById('groups-status');
    if (statusEl) statusEl.textContent = 'Se încarcă grupele...';

    try {
        const resp = await fetch('/api/groups');
        if (!resp.ok) throw new Error('Eroare ' + resp.status + ' la /api/groups');
        const data = await resp.json();
        renderTable('groups-table', data);
        if (statusEl) statusEl.textContent = 'S-au încărcat ' + (Array.isArray(data) ? data.length : 0) + ' grupe.';
    } catch (e) {
        console.error(e);
        if (statusEl) statusEl.textContent = 'Eroare la încărcare: ' + e.message;
    }
}
// ===== Asistenta LOGIN =====
function setLoginMsg(text, type = "") {
    const el = document.getElementById("loginMsg");
    if (!el) return;
    el.textContent = text;
    el.classList.remove("ok", "err");
    if (type) el.classList.add(type);
}

async function loginDancer() {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    const res = await fetch("/api/dancer/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
        credentials: "include"   // IMPORTANT cookie sesiune
    });

    if (res.ok) {
        const meRes = await fetch("/api/dancer/me", { credentials: "include" });
        if (meRes.ok) {
            const usernameFromSession = await meRes.text();
            document.getElementById("dancerName").textContent = usernameFromSession;
        }
        showPrivateSections();

        await onDancerLoggedInExtras();
    } else {
        const msg = await res.text();
        setLoginMsg(msg || ("Eroare " + res.status), "err");
    }
}

async function logout() {
    try {
        await fetch("/api/dancer/logout", {
            method: "POST",
            credentials: "include"
        });

        // reincarcare pagina sau redirectionare
        window.location.reload();
        // sau: window.location.href = "dancer.html";
    } catch (e) {
        console.error("Logout error:", e);
    }
}


// ===== INIT =====
document.addEventListener("DOMContentLoaded", () => {
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
    const loginBtn = document.getElementById("loginBtn");
    if (loginBtn) loginBtn.addEventListener("click", loginDancer);

    // comutare tabel dansatori
    const btn = document.getElementById("toggleViewDancersBtn");
    const table = document.getElementById("dancers-table");
    if (btn && table) {
        btn.addEventListener("click", async () => {
            // daca e ascuns -> il arat si incarc
            if (table.classList.contains("hidden")) {
                try {
                    await loadDancers();
                    table.classList.remove("hidden");
                    btn.textContent = "Ascunde dansatorii";
                } catch (e) {
                    console.error(e);
                    const statusEl = document.getElementById('dancers-status');
                    if (statusEl) statusEl.textContent = "Eroare: " + e.message;
                }
            } else {
                table.classList.add("hidden");
                btn.textContent = "Încarcă lista dansatorilor";
            }
        });
    }

    //comutare grupe
    const btnGroup = document.getElementById("toggleViewGroupsBtn");
    const tableGroups = document.getElementById("groups-table");

    if (btnGroup && tableGroups) {
        btnGroup.addEventListener("click", async () => {

            const isHidden = tableGroups.classList.contains("hidden");

            if (isHidden) {
                try {
                    await loadGroupsIntoTable();          // <-- functia ta existenta
                    tableGroups.classList.remove("hidden");
                    btnGroup.textContent = "Ascunde grupele";
                } catch (e) {
                    console.error(e);
                    const statusEl = document.getElementById("groups-status");
                    if (statusEl) statusEl.textContent = "Eroare: " + e.message;
                }
            } else {
                tableGroups.classList.add("hidden");
                btnGroup.textContent = "Încarcă grupele din /api/groups";
            }
        });
    }


});


function openFRDS() {

    const url = `https://app.dancesport.ro`;
    window.open(url);
}

async function changePassword() {
    const oldPassword = document.getElementById("oldPass").value;
    const newPassword = document.getElementById("newPass").value;
    const confirmPassword = document.getElementById("confirmNewPass").value;

    const msg = document.getElementById("changePassMsg");

    // VALIDARE FRONTEND
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

    // optional: regula minima
    if (newPassword.length < 6) {
        msg.textContent = "Parola trebuie sa aiba minim 6 caractere.";
        msg.className = "err";
        return;
    }

    // TRIMITEM LA BACKEND
    const res = await fetch("/api/dancer/change-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            oldPassword,
            newPassword
        }),
        credentials: "include"
    });

    if (res.ok) {
        msg.textContent = "Parola a fost schimbata cu succes.";
        msg.className = "ok";

        // optional: golim campurile
        document.getElementById("oldPass").value = "";
        document.getElementById("newPass").value = "";
        document.getElementById("confirmNewPass").value = "";
    } else {
        msg.textContent = "Parola veche este gresita.";
        msg.className = "err";
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const toggleText = document.getElementById("toggleChangePass");
    const box = document.getElementById("changePasswordBox");

    if (toggleText && box) {
        toggleText.addEventListener("click", () => {
            box.classList.toggle("hidden");
        });
    }
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) logoutBtn.addEventListener("click", logout);
});


document.getElementById("changePassBtn")
    ?.addEventListener("click", changePassword);

// ================== ORE PRIVATE: asistenta UI ==================
function setPrMsg(text, type = "") {
    const el = document.getElementById("prMsg");
    if (!el) return;
    el.textContent = text || "";
    el.classList.remove("ok", "err");
    if (type) el.classList.add(type);
}

function dayName(d) {
    return ["", "Luni", "Marti", "Miercuri", "Joi", "Vineri", "Sambata", "Duminica"][d] || ("Zi " + d);
}

// ================== Disponibilitate ==================
async function loadAvailability() {
    const table = document.getElementById("availTable");
    if (!table) return;

    const res = await fetch("/api/dancer/availability", { credentials: "include" });
    if (!res.ok) {
        const txt = await res.text();
        table.innerHTML = "";
        return setPrMsg(txt || ("Eroare availability: " + res.status), "err");
    }

    const data = await res.json();
    renderAvailabilityTable(data);
}

function renderAvailabilityTable(data) {
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
            await deleteAvailability(row.id);
            await loadAvailability();
        };
        tdA.appendChild(btn);
        tr.appendChild(tdA);

        tbody.appendChild(tr);
    });

    table.appendChild(tbody);
}

async function addAvailability() {
    const dayOfWeek = parseInt(document.getElementById("availDay").value, 10);
    const startTime = document.getElementById("availStart").value;
    const endTime = document.getElementById("availEnd").value;

    if (!startTime || !endTime) return setPrMsg("Completeaza start/end.", "err");
    if (startTime >= endTime) return setPrMsg("Start trebuie < End.", "err");

    const res = await fetch("/api/dancer/availability", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ dayOfWeek, startTime, endTime })
    });

    const txt = await res.text();
    if (!res.ok) return setPrMsg(txt || ("Eroare add availability: " + res.status), "err");

    setPrMsg("Disponibilitate salvata.", "ok");
}

async function deleteAvailability(id) {
    const res = await fetch("/api/dancer/availability/" + id, {
        method: "DELETE",
        credentials: "include"
    });

    if (!(res.ok || res.status === 204)) {
        const txt = await res.text();
        return setPrMsg(txt || ("Eroare stergere availability: " + res.status), "err");
    }

    setPrMsg("Interval sters.", "ok");
}

// ================== Cerere ore private ==================
let coachesCache = [];
let prefsCache = []; // [{coachId, coachName, hours}]

async function loadCoachesForPrefs() {
    const sel = document.getElementById("prefCoach");
    if (!sel) return;

    const res = await fetch("/api/coaches");
    if (!res.ok) {
        const txt = await res.text();
        return setPrMsg(txt || ("Nu pot incarca antrenorii: " + res.status), "err");
    }

    coachesCache = await res.json();

    sel.innerHTML = "";
    coachesCache.forEach(c => {
        const opt = document.createElement("option");
        opt.value = c.id;
        opt.textContent = `${c.id}. ${c.name}`;
        sel.appendChild(opt);
    });
}

function renderPrefsTable() {
    const table = document.getElementById("prefTable");
    table.innerHTML = "";

    const thead = document.createElement("thead");
    const hr = document.createElement("tr");
    ["coach", "ore", "actiuni"].forEach(h => {
        const th = document.createElement("th");
        th.textContent = h;
        hr.appendChild(th);
    });
    thead.appendChild(hr);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");

    if (prefsCache.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 3;
        td.textContent = "Nu ai preferinte adaugate.";
        tr.appendChild(td);
        tbody.appendChild(tr);
        table.appendChild(tbody);
        return;
    }

    prefsCache.forEach((p, idx) => {
        const tr = document.createElement("tr");

        const tdCoach = document.createElement("td");
        tdCoach.textContent = p.coachName ?? ("#" + p.coachId);
        tr.appendChild(tdCoach);

        const tdH = document.createElement("td");
        tdH.textContent = p.hours;
        tr.appendChild(tdH);

        const tdA = document.createElement("td");
        const btn = document.createElement("button");
        btn.textContent = "Sterge";
        btn.onclick = () => {
            prefsCache.splice(idx, 1);
            renderPrefsTable();
        };
        tdA.appendChild(btn);
        tr.appendChild(tdA);

        tbody.appendChild(tr);
    });

    table.appendChild(tbody);
}

function addPreferenceLocal() {
    const coachId = parseInt(document.getElementById("prefCoach").value, 10);
    const hours = parseInt(document.getElementById("prefHours").value, 10);

    if (!coachId || coachId <= 0) return setPrMsg("Alege antrenor.", "err");
    if (!hours || hours <= 0) return setPrMsg("Ore invalide.", "err");

    // nu permitem acelasi coach de 2 ori
    if (prefsCache.some(p => p.coachId === coachId)) {
        return setPrMsg("Ai adaugat deja acest antrenor. Sterge-l si adauga din nou cu alte ore.", "err");
    }

    const coach = coachesCache.find(c => c.id === coachId);
    prefsCache.push({ coachId, coachName: coach?.name, hours });

    renderPrefsTable();
    setPrMsg("", "");
}

async function loadMyPrivateRequest() {
    const res = await fetch("/api/dancer/private-request", { credentials: "include" });

    if (res.status === 404) {
        // nu exista inca cerere
        prefsCache = [];
        renderPrefsTable();
        return;
    }

    if (!res.ok) {
        const txt = await res.text();
        return setPrMsg(txt || ("Eroare request: " + res.status), "err");
    }

    const data = await res.json();

    // seteaza totalHours
    const totalEl = document.getElementById("totalHours");
    if (totalEl) totalEl.value = data.totalHours ?? 1;

    // incarca preferinte in cache
    prefsCache = (data.preferences || []).map(p => {
        const coach = coachesCache.find(c => c.id === p.coachId);
        return { coachId: p.coachId, coachName: coach?.name, hours: p.hours };
    });

    renderPrefsTable();
}

async function saveMyPrivateRequest() {
    const totalHours = parseInt(document.getElementById("totalHours").value, 10);
    if (!totalHours || totalHours <= 0) return setPrMsg("Total ore invalide.", "err");

    const preferences = prefsCache.map(p => ({ coachId: p.coachId, hours: p.hours }));

    const res = await fetch("/api/dancer/private-request", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ totalHours, preferences })
    });

    const txt = await res.text();
    if (!res.ok) return setPrMsg(txt || ("Eroare salvare: " + res.status), "err");

    setPrMsg("Cererea a fost salvata.", "ok");
}

// ================== Hook in UI (doar dupa login) ==================
function initPrivateLessonsUI() {
    // butoane disponibilitate
    document.getElementById("addAvailBtn")?.addEventListener("click", async () => {
        await addAvailability();
        await loadAvailability();
    });

    // butoane cerere
    document.getElementById("addPrefBtn")?.addEventListener("click", () => {
        addPreferenceLocal();
    });

    document.getElementById("saveRequestBtn")?.addEventListener("click", async () => {
        await saveMyPrivateRequest();
    });
}

async function onDancerLoggedInExtras() {
    initPrivateLessonsUI();
    await loadCoachesForPrefs();
    await loadAvailability();
    await loadMyPrivateRequest();
}
