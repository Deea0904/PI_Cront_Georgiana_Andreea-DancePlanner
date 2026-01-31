document.getElementById('add-group-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('group-name').value.trim();
    const description = document.getElementById('group-description').value.trim();
    const msgEl = document.getElementById('msg');

    if (!name) { alert("Numele este obligatoriu"); return; }

    try {
        const resp = await fetch('/api/groups', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, description })
        });
        if (resp.ok) {
            msgEl.textContent = 'Grupă adăugată!';
            msgEl.className = 'msg ok';
            e.target.reset();
        } else {
            msgEl.className = 'msg err';
            msgEl.textContent = 'Eroare la salvare.';
        }
    } catch (err) { msgEl.className = 'msg err'; }
});