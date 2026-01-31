// =====  tabel generic cu ordinea id, name, description, restul =====
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

// ===== API PAGINA ANTRENOR =====
async function loadCoaches() {
    const statusEl = document.getElementById('coach-status');
    if (statusEl) statusEl.textContent = 'Se încarcă antrenorii...';

    try {
        const resp = await fetch('/api/coaches');
        if (!resp.ok) throw new Error('Eroare ' + resp.status + ' la /api/coaches');
        const data = await resp.json();
        renderTable('coach-table', data);
        if (statusEl) statusEl.textContent = 'S-au încărcat ' + (Array.isArray(data) ? data.length : 0) + ' antrenori.';
    } catch (e) {
        console.error(e);
        if (statusEl) statusEl.textContent = 'Eroare la încărcare: ' + e.message;
    }
}

async function createCoach() {
    const name = document.getElementById('coach-name').value.trim();
    const description = document.getElementById('coach-description').value.trim();

    if (!name) { alert('Completează numele coach-ului.'); return; }

    const body = { name, description: description || null };

    try {
        const resp = await fetch('/api/coaches', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!resp.ok) throw new Error('Eroare ' + resp.status + ' la POST /api/coaches');

        document.getElementById('coach-name').value = '';
        document.getElementById('coach-description').value = '';
        await loadCoaches();
    } catch (e) {
        console.error(e);
        alert('Eroare la salvare coach: ' + e.message);
    }
}

async function deleteCoach() {
    const id = document.getElementById('coach-delete-id').value;
    if (!id) { alert('Completează ID-ul coach-ului de șters.'); return; }
    if (!confirm('Sigur vrei să ștergi coach cu id=' + id + '?')) return;

    try {
        const resp = await fetch('/api/coaches/' + id, { method: 'DELETE' });
        if (!resp.ok && resp.status !== 204) {
            throw new Error('Eroare ' + resp.status + ' la DELETE /api/coaches/' + id);
        }
        document.getElementById('coach-delete-id').value = '';
        await loadCoaches();
    } catch (e) {
        console.error(e);
        alert('Eroare la ștergere coach: ' + e.message);
    }
}

// ===== GROUPS API (folosit si pe dancer, si pe dropdown) =====
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

async function createGroup() {
    const name = document.getElementById('group-name').value.trim();
    const description = document.getElementById('group-description').value.trim();

    if (!name) { alert('Completează numele grupei.'); return; }

    const body = { name, description: description || null };

    try {
        const resp = await fetch('/api/groups', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!resp.ok) throw new Error('Eroare ' + resp.status + ' la POST /api/groups');

        document.getElementById('group-name').value = '';
        document.getElementById('group-description').value = '';

        await loadGroupsIntoTable();
        await loadLevelNamesFromGroups();
    } catch (e) {
        console.error(e);
        alert('Eroare la salvare grupă: ' + e.message);
    }
}

async function deleteGroup() {
    const id = document.getElementById('group-delete-id').value;
    if (!id) { alert('Completează ID-ul grupei de șters.'); return; }
    if (!confirm('Sigur vrei să ștergi grupa cu id=' + id + '?')) return;

    try {
        const resp = await fetch('/api/groups/' + id, { method: 'DELETE' });
        if (!resp.ok && resp.status !== 204) {
            throw new Error('Eroare ' + resp.status + ' la DELETE /api/groups/' + id);
        }
        document.getElementById('group-delete-id').value = '';
        await loadGroupsIntoTable();
        await loadLevelNamesFromGroups();
    } catch (e) {
        console.error(e);
        alert('Eroare la ștergere grupă: ' + e.message);
    }
}

// ===== dropdown levelName din /api/groups (pentru Dancer) =====
async function loadLevelNamesFromGroups() {
    const select = document.getElementById('dancer-levelName');
    if (!select) return;

    select.innerHTML = '<option value="">Se încarcă...</option>';

    try {
        const resp = await fetch('/api/groups');
        if (!resp.ok) throw new Error('Eroare ' + resp.status + ' la /api/groups');
        const groups = await resp.json();

        if (!Array.isArray(groups) || groups.length === 0) {
            select.innerHTML = '<option value="">Nu există groups</option>';
            return;
        }

        select.innerHTML = '<option value="">-- alege level --</option>';

        groups.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g.name;
            opt.textContent = g.name;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error('Eroare loadLevelNamesFromGroups:', e);
        select.innerHTML = '<option value="">Eroare la încărcare</option>';
    }
}

// DANCERS API
async function loadDancers() {
    const statusEl = document.getElementById('dancers-status');
    if (statusEl) statusEl.textContent = 'Se încarcă dancerii...';

    try {
        const resp = await fetch('/api/dancers');
        if (!resp.ok) throw new Error('Eroare ' + resp.status + ' la /api/dancers');
        const data = await resp.json();
        const hidden = new Set(["levelId"]); // aici pui ce nu vrei sa apara
        const filtered = Array.isArray(data)
            ? data.map(d => Object.fromEntries(Object.entries(d).filter(([k]) => !hidden.has(k))))
            : data;

        renderTable('dancers-table', filtered);
        if (statusEl) statusEl.textContent = 'S-au încărcat ' + (Array.isArray(data) ? data.length : 0) + ' dansatori.';
    } catch (e) {
        console.error(e);
        if (statusEl) statusEl.textContent = 'Eroare la încărcare: ' + e.message;
    }
}

async function createDancer() {
    const name = document.getElementById('dancer-name').value.trim();
    const ageInput = document.getElementById('dancer-age').value;
    const levelName = document.getElementById('dancer-levelName').value;

    if (!name) { alert('Completează numele dancer-ului.'); return; }
    if (!levelName) { alert('Alege un level din listă.'); return; }
    if (!ageInput) { alert('Introdu varsta dansatorului'); return; }

    const age = parseInt(ageInput, 10);
    if (Number.isNaN(age) || age <= 0) {
        alert('Varsta trebuie sa fie un numar pozitiv.');
        return;
    }

    const body = { name, age, levelName };

    try {
        const resp = await fetch('/api/dancers', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!resp.ok) throw new Error('Eroare ' + resp.status + ' la POST /api/dancers');

        document.getElementById('dancer-name').value = '';
        document.getElementById('dancer-age').value = '';
        document.getElementById('dancer-levelName').value = '';

        await loadDancers();
    } catch (e) {
        console.error(e);
        alert('Eroare la salvare dancer: ' + e.message);
    }
}


async function deleteDancer() {
    const id = document.getElementById('dancer-delete-id').value;
    if (!id) { alert('Completează ID-ul dancer-ului de șters.'); return; }
    if (!confirm('Sigur vrei să ștergi dancer cu id=' + id + '?')) return;

    try {
        const resp = await fetch('/api/dancers/' + id, { method: 'DELETE' });
        if (!resp.ok && resp.status !== 204) {
            throw new Error('Eroare ' + resp.status + ' la DELETE /api/dancers/' + id);
        }
        document.getElementById('dancer-delete-id').value = '';
        await loadDancers();
    } catch (e) {
        console.error(e);
        alert('Eroare la ștergere dancer: ' + e.message);
    }
}


async function loginDancer() {
    const username = document.getElementById("dancerNameLogin").value.trim();
    const password = document.getElementById("dancerPassword").value;

    const status = document.getElementById("login-status");
    status.textContent = "Se autentifica...";

    const body = new URLSearchParams();
    body.append("username", username);
    body.append("password", password);

    const res = await fetch("/dancer/login", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: body.toString(),
        credentials: "include"
    });

    if (res.ok) {
        status.textContent = "Login reusit!";
        showPrivateSections();
    } else {
        status.textContent = "Login esuat (user/parola gresite).";
    }
}

function showPrivateSections() {
    document.getElementById("loginBox").style.display = "none";
    document.getElementById("privateContent").style.display = "block";
}


async function loadTimetable() {
    const statusEl = document.getElementById('tt-status');
    if (statusEl) statusEl.textContent = 'Se încarcă orarul...';

    try {
        const resp = await fetch('/api/timetable');
        if (!resp.ok) throw new Error('Eroare ' + resp.status + ' la /api/timetable');

        const data = await resp.json();

        renderTable('tt-table', data);

        if (statusEl) {
            statusEl.textContent = 'S-au încărcat ' + (Array.isArray(data) ? data.length : 0) + ' intrări de orar.';
        }
    } catch (e) {
        console.error(e);
        if (statusEl) statusEl.textContent = 'Eroare la încărcare: ' + e.message;
    }
}

