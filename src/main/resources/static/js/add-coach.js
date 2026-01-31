document.getElementById('add-coach-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const nameInput = document.getElementById('coach-name');
    const name = nameInput.value.trim();
    const msgEl = document.getElementById('msg');

    // Resetare mesaje
    msgEl.textContent = '';
    msgEl.className = 'msg';

    if (!name) {
        msgEl.textContent = 'Te rog completează numele.';
        msgEl.classList.add('err');
        return;
    }

    try {
        const resp = await fetch('/api/coaches', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });

        if (!resp.ok) {
            msgEl.textContent = `Eroare la salvare (status ${resp.status}).`;
            msgEl.classList.add('err');
            return;
        }

        const created = await resp.json();
        msgEl.textContent = `Coach adăugat cu succes! (ID: ${created.id})`;
        msgEl.classList.add('ok');

        // Curatare input
        nameInput.value = '';

    } catch (err) {
        console.error("Network error:", err);
        msgEl.textContent = 'Eroare de rețea. Verifică dacă serverul este pornit.';
        msgEl.classList.add('err');
    }
});