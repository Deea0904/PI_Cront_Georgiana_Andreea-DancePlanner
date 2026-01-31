const toggleLoginBtn = document.getElementById('toggleLoginBtn');
const loginBox = document.getElementById('loginBox');
const loginBtn = document.getElementById('loginBtn');
const loginMsg = document.getElementById('loginMsg');

const dancerContent = document.getElementById('dancerContent');
const viewDancersBtn = document.getElementById('viewDancersBtn');
const dancersResult = document.getElementById('dancersResult');

// arata / ascunde formularul
toggleLoginBtn.addEventListener('click', () => {
    loginBox.classList.toggle('hidden');
    loginMsg.textContent = '';
});

// login dancer
loginBtn.addEventListener('click', async () => {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    if (!username || !password) {
        loginMsg.textContent = 'Completeaza username si parola';
        return;
    }

    try {
        const res = await fetch('/api/dancer/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!res.ok) {
            loginMsg.textContent = 'Login esuat';
            return;
        }

        const data = await res.json();
        localStorage.setItem('dancer_token', data.token);

        loginMsg.textContent = 'Login reusit!';
        loginBox.classList.add('hidden');
        dancerContent.classList.remove('hidden');
        toggleLoginBtn.disabled = true;

    } catch (err) {
        loginMsg.textContent = 'Eroare server';
        console.error(err);
    }
});

// vezi dansatorii
viewDancersBtn.addEventListener('click', async () => {
    dancersResult.textContent = 'Se incarca...';

    const token = localStorage.getItem('dancer_token');

    const res = await fetch('/api/dancers', {
        headers: {
            'Authorization': 'Bearer ' + token
        }
    });

    const dancers = await res.json();

    dancersResult.innerHTML = `
    <ul>
      ${dancers.map(d => `<li>${d.id} - ${d.name}</li>`).join('')}
    </ul>
  `;
});
