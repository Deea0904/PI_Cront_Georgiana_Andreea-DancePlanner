async function loadGroups() {
    const select = document.getElementById('group-select');
    try {
        const resp = await fetch('/api/groups');
        if (!resp.ok) return;
        const groups = await resp.json();
        select.innerHTML = `<option value="">-- alege o grupa --</option>`;
        groups.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g.name;
            opt.textContent = `${g.name} (${g.description || "fără descriere"})`;
            select.appendChild(opt);
        });
    } catch (e) { console.error(e); }
}

document.getElementById('add-dancer-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const msgEl = document.getElementById('msg');
    const name = document.getElementById('dancer-name').value.trim();
    const age = parseInt(document.getElementById('dancer-age').value, 10);
    const levelName = document.getElementById('group-select').value;

    try {
        const resp = await fetch('/api/dancers', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, age, levelName })
        });
        if (resp.ok) {
            msgEl.textContent = 'Dancer adăugat cu succes!';
            msgEl.className = 'msg ok';
            e.target.reset();
        } else {
            msgEl.textContent = 'Eroare la salvare.';
            msgEl.className = 'msg err';
        }
    } catch (e) { msgEl.className = 'msg err'; msgEl.textContent = 'Eroare de rețea.'; }
});

loadGroups();