function setMsg(t, type="") {
    const el = document.getElementById("msg");
    el.textContent = t;
    el.classList.remove("ok", "err");
    if (type) el.classList.add(type);
}

document.getElementById("loginBtn").addEventListener("click", async () => {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value;

    const res = await fetch("/api/admin/login", {
        method: "POST",
        headers: {"Content-Type":"application/json"},
        credentials: "include",
        body: JSON.stringify({username, password})
    });

    if (res.ok) {
        window.location.href = "admin.html";
    } else {
        const txt = await res.text();
        setMsg(txt || ("Eroare " + res.status), "err");
    }
});
