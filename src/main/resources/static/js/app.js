const API = "/api";
const token = () => sessionStorage.getItem("skilljobs_token");
const user = () =>
    JSON.parse(sessionStorage.getItem("skilljobs_user") || "null");
const esc = (v) =>
    String(v ?? "").replace(
        /[&<>"']/g,
        (c) =>
            ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
            c
            ],
    );
async function api(path, opts = {}) {
    const headers = { ...(opts.headers || {}) };
    if (token()) headers.Authorization = "Bearer " + token();
    if (opts.body && !(opts.body instanceof FormData))
        headers["Content-Type"] = "application/json";
    const res = await fetch(API + path, { ...opts, headers });
    if (res.status === 204) return null;
    let data;
    try {
        data = await res.json();
    } catch {
        data = { message: res.statusText };
    }
    if (!res.ok)
        throw new Error(
            data.message || data.error || `Request failed (${res.status})`,
        );
    return data;
}

async function openProtectedResume(url) {
    const newTab = window.open("about:blank", "_blank");

    if (!newTab) {
        notice("Please allow pop-ups to preview the resume.", "error");
        return;
    }

    try {
        const response = await fetch(url, {
            method: "GET",
            headers: {
                Authorization: "Bearer " + token()
            }
        });

        if (!response.ok) {
            throw new Error("Unable to open resume. HTTP " + response.status);
        }

        const blob = await response.blob();
        const disposition = response.headers.get("Content-Disposition") || "";

        let filename = "resume";

        const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i);
        const normalMatch = disposition.match(/filename="?([^";]+)"?/i);

        if (utf8Match) {
            filename = decodeURIComponent(utf8Match[1]);
        } else if (normalMatch) {
            filename = normalMatch[1];
        }

        const fileType = blob.type.toLowerCase();
        const lowerName = filename.toLowerCase();

        // PDF preview
        if (fileType.includes("pdf") || lowerName.endsWith(".pdf")) {
            const fileUrl = URL.createObjectURL(blob);
            newTab.location.href = fileUrl;

            setTimeout(() => URL.revokeObjectURL(fileUrl), 300000);
            return;
        }

        // DOCX preview
        if (
            fileType.includes("wordprocessingml") ||
            lowerName.endsWith(".docx")
        ) {
            const previewDoc = newTab.document;

            previewDoc.open();
            previewDoc.write(`<!DOCTYPE html>
                <html>
                <head>
                    <title>Resume Preview</title>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <style>
                        body {
                            margin: 0;
                            font-family: Arial, sans-serif;
                            background: #f1f3f5;
                        }
                        header {
                            padding: 15px 24px;
                            background: #ffffff;
                            border-bottom: 1px solid #ddd;
                            font-weight: bold;
                        }
                        #docx-container {
                            padding: 25px 10px;
                            overflow: auto;
                        }
                    </style>
                </head>
                <body>
                    <header>Resume Preview</header>
                    <main id="docx-container">Loading resume...</main>
                </body>
                </html>`);
            previewDoc.close();

            function loadScript(src) {
                return new Promise((resolve, reject) => {
                    const script = previewDoc.createElement("script");
                    script.src = src;
                    script.onload = resolve;
                    script.onerror = () => reject(
                        new Error("Unable to load DOCX preview library.")
                    );
                    previewDoc.head.appendChild(script);
                });
            }

            await loadScript(
                "https://cdn.jsdelivr.net/npm/jszip@3.10.1/dist/jszip.min.js"
            );

            await loadScript(
                "https://cdn.jsdelivr.net/npm/docx-preview@0.4.1/dist/docx-preview.min.js"
            );

            if (!newTab.docx || !newTab.docx.renderAsync) {
                throw new Error("DOCX preview library is unavailable.");
            }

            await newTab.docx.renderAsync(
                blob,
                previewDoc.getElementById("docx-container"),
                previewDoc.head,
                {
                    renderAltChunks: false
                }
            );

            return;
        }

        // Download unsupported document types
        newTab.close();

        const fileUrl = URL.createObjectURL(blob);
        const link = document.createElement("a");

        link.href = fileUrl;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        link.remove();

        setTimeout(() => URL.revokeObjectURL(fileUrl), 60000);

        notice("Resume downloaded successfully.");

    } catch (error) {
        if (!newTab.closed) {
            newTab.close();
        }

        notice(error.message, "error");
    }
}

function showError(el, msg) {
    if (el) {
        el.textContent = msg;
        el.classList.remove("hidden");
    }
}
function notice(msg, type = "success") {
    const n = document.getElementById("notice");
    if (n) {
        n.textContent = msg;
        n.className = "notice " + type;
        n.classList.remove("hidden");
        window.scrollTo({ top: 0, behavior: "smooth" });
        setTimeout(() => n.classList.add("hidden"), 4500);
    }
}
function logout() {
    sessionStorage.removeItem("skilljobs_token");
    sessionStorage.removeItem("skilljobs_user");
    location.href = "/";
}
function money(a, b) {
    if (a == null && b == null) return "Salary not disclosed";
    const f = (n) => "₹" + Number(n).toLocaleString("en-IN");
    return `${a == null ? "" : f(a)}${a != null && b != null ? " – " : ""}${b == null ? "" : f(b)}`;
}
function statusPill(s) {
    return `<span class="pill ${String(s).toLowerCase()}">${esc(String(s).replaceAll("_", " "))}</span>`;
}
function empty(msg) {
    return `<div class="empty"><div>◇</div><b>${esc(msg)}</b><p>There is nothing to show here yet.</p></div>`;
}
function card(title, sub, body) {
    return `<section class="panel"><div class="panel-head"><div><h2>${title}</h2><p>${sub || ""}</p></div></div>${body}</section>`;
}

// Authentication pages
const loginForm = document.getElementById("loginForm");
if (loginForm)
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const error = document.getElementById("error");
        error.textContent = "Signing in…";
        try {
            const f = new FormData(loginForm);
            const d = await api("/auth/login", {
                method: "POST",
                body: JSON.stringify({
                    email: f.get("email"),
                    password: f.get("password"),
                }),
            });
            sessionStorage.setItem("skilljobs_token", d.token);
            sessionStorage.setItem(
                "skilljobs_user",
                JSON.stringify({
                    id: d.userId,
                    name: d.fullName,
                    email: d.email,
                    role: d.role,
                }),
            );
            location.href = "/dashboard.html";
        } catch (x) {
            showError(error, x.message);
        }
    });
const registerForm = document.getElementById("registerForm");
if (registerForm)
    registerForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const error = document.getElementById("error");
        error.textContent = "Creating account…";
        try {
            const f = new FormData(registerForm);
            const d = await api("/auth/register", {
                method: "POST",
                body: JSON.stringify({
                    fullName: f.get("fullName"),
                    email: f.get("email"),
                    password: f.get("password"),
                    role: f.get("role"),
                }),
            });
            sessionStorage.setItem("skilljobs_token", d.token);
            sessionStorage.setItem(
                "skilljobs_user",
                JSON.stringify({
                    id: d.userId,
                    name: d.fullName,
                    email: d.email,
                    role: d.role,
                }),
            );
            location.href = "/dashboard.html";
        } catch (x) {
            showError(error, x.message);
        }
    });

// Dashboard shell
const view = document.getElementById("view");
if (view) {
    const me = user();
    if (!token() || !me) {
        location.href = "/login.html";
    } else initDashboard(me);
}
async function initDashboard(me) {
    document.getElementById("whoami").textContent = me.name;
    document.getElementById("sideName").textContent = me.name;
    document.getElementById("sideRole").textContent = me.role;
    document.getElementById("avatar").textContent = (me.name || "U")
        .slice(0, 1)
        .toUpperCase();
    document.getElementById("logoutBtn").onclick = logout;
    const items =
        me.role === "ADMIN"
            ? [
                ["overview", "Overview"],
                ["users", "Users"],
                ["companies", "Companies"],
                ["jobs", "Job moderation"],
                ["applications", "Applications"],
                ["skills", "Skills"],
            ]
            : me.role === "RECRUITER"
                ? [
                    ["overview", "Overview"],
                    ["companies", "My companies"],
                    ["jobs", "Manage jobs"],
                    ["applications", "Applicants"],
                ]
                : [
                    ["overview", "Overview"],
                    ["jobs", "Find jobs"],
                    ["profile", "My profile"],
                    ["applications", "My applications"],
                    ["saved", "Saved jobs"],
                ];
    document.getElementById("nav").innerHTML = items
        .map(
            (x, i) =>
                `<button class="nav-item ${i === 0 ? "active" : ""}" data-view="${x[0]}"><span>${["◈", "▦", "◇", "↗", "◎"][i % 5]}</span>${x[1]}</button>`,
        )
        .join("");
    document.getElementById("nav").addEventListener("click", (e) => {
        const b = e.target.closest("[data-view]");
        if (!b) return;
        document
            .querySelectorAll(".nav-item")
            .forEach((x) => x.classList.toggle("active", x === b));
        renderView(me, b.dataset.view);
    });
    renderView(me, "overview");
}
async function renderView(me, section) {
    const titleMap = {
        overview: "Overview",
        jobs: me.role === "CANDIDATE" ? "Explore opportunities" : "Manage jobs",
        profile: "My profile",
        applications:
            me.role === "CANDIDATE"
                ? "My applications"
                : me.role === "ADMIN"
                    ? "Applications"
                    : "Applicants",
        saved: "Saved jobs",
        companies: me.role === "ADMIN" ? "Companies" : "My companies",
        users: "User management",
        skills: "Skill catalogue",
    };
    document.getElementById("pageTitle").textContent =
        titleMap[section] || section;
    document.getElementById("pageSubtitle").textContent =
        me.role === "ADMIN"
            ? "Monitor and manage the SkillJobs platform."
            : me.role === "RECRUITER"
                ? "Manage your hiring workflow."
                : "Build your profile and find your next opportunity.";
    view.innerHTML = '<div class="loading">Loading workspace…</div>';
    try {
        if (me.role === "ADMIN") return await renderAdmin(section);
        if (me.role === "RECRUITER") return await renderRecruiter(section);
        return await renderCandidate(section);
    } catch (e) {
        view.innerHTML = `<div class="notice error">${esc(e.message)}</div>`;
    }
}
function metric(label, value, caption) {
    return `<div class="metric"><span>${label}</span><strong>${value}</strong><small>${caption || ""}</small></div>`;
}
async function renderCandidate(section) {
    if (section === "overview") {
        const [profile, apps, recommended, saved] = await Promise.all([
            api("/users/profile"),
            api("/applications/my?page=0&size=5"),
            api("/jobs/recommended").catch(() => []),
            api("/saved-jobs?page=0&size=5"),
        ]);
        view.innerHTML = `<div class="metrics">${metric("Profile skills", profile.skills.length, "Skills in your profile")}${metric("Applications", apps.totalElements ?? apps.content?.length ?? 0, "Submitted applications")}${metric("Recommended jobs", recommended.length, "Based on your skills")}${metric("Saved jobs", saved.totalElements ?? 0, "Keep opportunities handy")}</div>${card("Recommended for you", "Jobs ranked by your skill match", jobCards(recommended.slice(0, 4)))}${card("Recent applications", "Latest application activity", applicationTable(apps.content || []))}`;
// Attach Apply button handlers in Overview
view.querySelectorAll("[data-apply]").forEach((b) =>
    (b.onclick = async () => {
        const coverLetter = prompt("Optional cover letter:") || "";

        try {
            await api("/applications/" + b.dataset.apply, {
                method: "POST",
                body: JSON.stringify({ coverLetter }),
            });
            notice("Application submitted successfully");
        } catch (e) {
            notice(e.message, "error");
        }
    }),
);

// Attach Save button handlers in Overview
view.querySelectorAll("[data-save]").forEach((b) =>
    (b.onclick = async () => {
        try {
            await api("/saved-jobs/" + b.dataset.save, {
                method: "POST",
            });
            notice("Job saved");
        } catch (e) {
            notice(e.message, "error");
        }
    }),
);
        view.querySelectorAll("[data-withdraw]").forEach(
            (b) =>
            (b.onclick = async () => {
                if (!confirm("Withdraw this application?")) return;
                await api("/applications/" + b.dataset.withdraw + "/withdraw", {
                    method: "PATCH",
                });
                notice("Application withdrawn");
                renderCandidate("overview");
            }),
        );
        return;
    }
    if (section === "jobs") {
        const skills = await api("/skills");
        view.innerHTML = card(
            "Find your next opportunity",
            "Search by role, company, skill or location",
            `<form id="jobSearch" class="filters"><input name="q" placeholder="Job title, company or skill"><input name="location" placeholder="Location"><select name="jobType"><option value="">All job types</option>${["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP"].map((x) => `<option>${x}</option>`).join("")}</select><input name="maxExperience" type="number" min="0" placeholder="Max experience"><select name="sort"><option value="createdAt,desc">Newest first</option><option value="salaryMax,desc">Highest salary</option><option value="title,asc">Title A–Z</option></select><button class="btn btn-primary">Search jobs</button></form><div id="jobResults"></div>`,
        );
        document.getElementById("jobSearch").onsubmit = (e) => {
            e.preventDefault();
            const f = new FormData(e.target);
            loadJobs(Object.fromEntries(f.entries()));
        };
        await loadJobs({});
        return;
    }
    if (section === "profile") {
        const p = await api("/users/profile"),
            skills = await api("/skills");
        view.innerHTML =
            card(
                "Personal information",
                "Keep your profile up to date",
                `<form id="profileForm" class="form-grid"><label>Full name<input name="fullName" required value="${esc(p.fullName)}"></label><label>Phone<input name="phone" value="${esc(p.phone || "")}"></label><label>Location<input name="location" value="${esc(p.location || "")}"></label><label class="wide">Professional headline<input name="headline" value="${esc(p.headline || "")}" placeholder="Java developer | Spring Boot | SQL"></label><div class="wide"><button class="btn btn-primary">Save profile</button></div></form>`,
            ) +
            card(
                "Your skills",
                "Add or remove skills to improve job matching",
                `<div class="skill-add"><select id="skillSelect"><option value="">Choose a skill</option>${skills.map((s) => `<option value="${s.id}">${esc(s.name)}</option>`).join("")}</select><button id="addSkill" class="btn btn-dark">Add skill</button></div><div class="chips">${p.skills
                    .map((n) => {
                        const found = skills.find((x) => x.name === n);
                        return `<span class="chip">${esc(n)} ${found ? `<button class="chip-remove" data-remove-skill="${found.id}" title="Remove skill">×</button>` : ""}</span>`;
                    })
                    .join("") || '<span class="muted">No skills added yet.</span>'
                }</div><div class="upload-box"><div><b>Resume</b><small>PDF, DOC or DOCX · Maximum 5 MB</small></div><form id="resumeForm"><input type="file" name="file" accept=".pdf,.doc,.docx" required><button class="btn btn-outline">Upload resume</button></form>${p.resumeUrl ? `<button type="button" class="btn btn-outline" id="viewResumeBtn">View uploaded resume</button>` : ""}</div>`,
            );
        document.getElementById("profileForm").onsubmit = async (e) => {
            e.preventDefault();
            const f = new FormData(e.target);
            await api("/users/profile", {
                method: "PUT",
                body: JSON.stringify(Object.fromEntries(f.entries())),
            });
            notice("Profile updated");
        };
        document.getElementById("addSkill").onclick = async () => {
            const id = document.getElementById("skillSelect").value;
            if (!id) return;
            await api("/users/skills/" + id, { method: "POST" });
            notice("Skill added");
            renderCandidate("profile");
        };
        view.querySelectorAll("[data-remove-skill]").forEach(
            (b) =>
            (b.onclick = async () => {
                await api("/users/skills/" + b.dataset.removeSkill, {
                    method: "DELETE",
                });
                notice("Skill removed");
                renderCandidate("profile");
            }),
        );
        document.getElementById("resumeForm").onsubmit = async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            await api("/users/resume", { method: "POST", body: fd });
            notice("Resume uploaded successfully");
            renderCandidate("profile");
        };
        const resumeButton = document.getElementById("viewResumeBtn");

        if (resumeButton) {
            resumeButton.onclick = () => openProtectedResume(p.resumeUrl);
        }
        return;
    }
    if (section === "applications") {
        const a = await api("/applications/my?page=0&size=50");
        view.innerHTML = card(
            "Application history",
            "Track the status of every role you applied for",
            applicationTable(a.content || []),
        );
        view.querySelectorAll("[data-withdraw]").forEach(
            (b) =>
            (b.onclick = async () => {
                if (!confirm("Withdraw this application?")) return;
                await api("/applications/" + b.dataset.withdraw + "/withdraw", {
                    method: "PATCH",
                });
                notice("Application withdrawn");
                renderCandidate("applications");
            }),
        );
        return;
    }
    if (section === "saved") {
        const s = await api("/saved-jobs?page=0&size=50");
        const rows = s.content || [];
        view.innerHTML = card(
            "Saved opportunities",
            "Jobs you bookmarked",
            rows.length
                ? `<div class="table-wrap"><table><thead><tr><th>Role</th><th>Company</th><th>Action</th></tr></thead><tbody>${rows.map((x) => `<tr><td>${esc(x.title)}</td><td>${esc(x.company)}</td><td><button class="btn btn-small btn-outline" data-unsave="${x.jobId}">Remove</button></td></tr>`).join("")}</tbody></table></div>`
                : empty("No saved jobs"),
        );
        view.querySelectorAll("[data-unsave]").forEach(
            (b) =>
            (b.onclick = async () => {
                await api("/saved-jobs/" + b.dataset.unsave, { method: "DELETE" });
                renderCandidate("saved");
            }),
        );
        return;
    }
}
let currentJobSearch = {};
let currentJobPage = 0;
async function loadJobs(params, reset = true) {
    if (reset) {
        currentJobSearch = params;
        currentJobPage = 0;
    }
    const q = new URLSearchParams(
        Object.entries(currentJobSearch).filter(([k, v]) => v !== "" && v != null),
    );
    q.set("page", String(currentJobPage));
    q.set("size", "10");
    const data = await api("/jobs?" + q.toString());
    const el = document.getElementById("jobResults");
    if (!el) return;
    el.innerHTML =
        jobCards(data.content || []) +
        `<div class="pagination"><button class="btn btn-outline btn-small" data-page="${Math.max(0, currentJobPage - 1)}" ${data.first ? "disabled" : ""}>Previous</button><span>Page ${data.number + 1} of ${Math.max(1, data.totalPages)}</span><button class="btn btn-outline btn-small" data-page="${currentJobPage + 1}" ${data.last ? "disabled" : ""}>Next</button></div>`;
    el.querySelectorAll("[data-apply]").forEach(
        (b) =>
        (b.onclick = async () => {
            const coverLetter = prompt("Optional cover letter:") || "";
            try {
                await api("/applications/" + b.dataset.apply, {
                    method: "POST",
                    body: JSON.stringify({ coverLetter }),
                });
                notice("Application submitted successfully");
            } catch (e) {
                notice(e.message, "error");
            }
        }),
    );
    el.querySelectorAll("[data-save]").forEach(
        (b) =>
        (b.onclick = async () => {
            try {
                await api("/saved-jobs/" + b.dataset.save, { method: "POST" });
                notice("Job saved");
            } catch (e) {
                notice(e.message, "error");
            }
        }),
    );
    el.querySelectorAll("[data-page]").forEach(
        (b) =>
        (b.onclick = () => {
            currentJobPage = Number(b.dataset.page);
            loadJobs(currentJobSearch, false);
        }),
    );
}
function jobCards(jobs) {
    if (!jobs?.length) return empty("No matching jobs found");
    return `<div class="job-grid">${jobs.map((j) => `<article class="job-card"><div class="job-card-top"><div class="company-logo">${esc((j.company || "C").slice(0, 1))}</div><div class="job-match">${j.matchPercentage || 0}% match</div></div><h3>${esc(j.title)}</h3><p class="company-name">${esc(j.company)} · ${esc(j.location || "Location not specified")}</p><p class="job-desc">${esc((j.description || "").slice(0, 150))}${(j.description || "").length > 150 ? "…" : ""}</p><div class="job-meta"><span>${esc(j.jobType || "")}</span><span>${esc(j.workMode || "")}</span><span>${j.experienceRequired ?? 0}+ yrs</span></div><div class="chips">${(j.requiredSkills || []).map((s) => `<span class="chip">${esc(s)}</span>`).join("")}</div><div class="match-detail"><b>Matched:</b> ${esc((j.matchedSkills || []).join(", ") || "None yet")}<br><b>Missing:</b> ${esc((j.missingSkills || []).join(", ") || "None")}</div><div class="job-card-foot"><span class="salary">${money(j.salaryMin, j.salaryMax)}</span><div><button class="btn btn-small btn-outline" data-save="${j.id}">Save</button> <button class="btn btn-small btn-primary" data-apply="${j.id}">Apply</button></div></div></article>`).join("")}</div>`;
}
function applicationTable(items) {
    if (!items.length) return empty("No applications yet");
    return `<div class="table-wrap"><table><thead><tr><th>Position</th><th>Company</th><th>Applied</th><th>Status</th><th>Action</th></tr></thead><tbody>${items.map((a) => `<tr><td>${esc(a.jobTitle)}</td><td>${esc(a.company)}</td><td>${esc((a.appliedAt || "").slice(0, 10))}</td><td>${statusPill(a.status)}</td><td>${a.status !== "HIRED" && a.status !== "REJECTED" && a.status !== "WITHDRAWN" ? `<button class="btn btn-small btn-outline" data-withdraw="${a.id}">Withdraw</button>` : "—"}</td></tr>`).join("")}</tbody></table></div>`;
}

async function renderRecruiter(section) {
    if (section === "overview") {
        const d = await api("/recruiter/dashboard");
        view.innerHTML = `<div class="metrics">${metric("Companies", d.companies, "Your organizations")}${metric("Jobs posted", d.jobs, "All job postings")}${metric("Open jobs", d.openJobs, "Currently accepting applications")}${metric("Applicants", d.applications, "Applications received")}</div>${card("Hiring snapshot", "A quick view of your recruitment activity", `<div class="callout">You have <b>${d.pending}</b> applications awaiting review. Open Applicants to review candidates and update their status.</div>`)}`;
        return;
    }
    if (section === "companies") {
        const mine = await api("/companies/mine");
        view.innerHTML = card(
            "Your companies",
            "Create the company profile used for job postings",
            `<form id="companyForm" class="form-grid"><label>Company name<input name="name" required></label><label>Website<input name="website" placeholder="https://example.com"></label><label>Location<input name="location"></label><label class="wide">Description<textarea name="description" rows="3"></textarea></label><div class="wide"><button class="btn btn-primary">Create company</button></div></form><div class="company-list">${mine.length ? mine.map((c) => `<div class="company-row"><div class="company-logo">${esc(c.name.slice(0, 1))}</div><div class="company-info"><b>${esc(c.name)}</b><small>${esc(c.location || "")} · ${esc(c.website || "")}</small></div><button class="btn btn-small btn-outline" data-edit-company="${c.id}">Edit</button></div>`).join("") : empty("No company profiles yet")}</div>`,
        );
        document.getElementById("companyForm").onsubmit = async (e) => {
            e.preventDefault();
            await api("/companies", {
                method: "POST",
                body: JSON.stringify(
                    Object.fromEntries(new FormData(e.target).entries()),
                ),
            });
            notice("Company created");
            renderRecruiter("companies");
        };
        view.querySelectorAll("[data-edit-company]").forEach(
            (b) =>
            (b.onclick = async () => {
                const c = mine.find((x) => String(x.id) === b.dataset.editCompany);
                const name = prompt("Company name:", c.name);
                if (name === null) return;
                const website = prompt("Website:", c.website || "");
                if (website === null) return;
                const location = prompt("Location:", c.location || "");
                if (location === null) return;
                const description = prompt("Description:", c.description || "");
                if (description === null) return;
                await api("/companies/" + c.id, {
                    method: "PUT",
                    body: JSON.stringify({ name, website, location, description }),
                });
                notice("Company updated");
                renderRecruiter("companies");
            }),
        );
        return;
    }
    if (section === "jobs") {
        const [companies, skills, mine] = await Promise.all([
            api("/companies/mine"),
            api("/skills"),
            api("/recruiter/jobs"),
        ]);
        view.innerHTML =
            card(
                "Post a job",
                "Create a new opportunity for candidates",
                companies.length
                    ? `<form id="jobForm" class="form-grid"><label>Job title<input name="title" required></label><label>Company<select name="companyId" required>${companies.map((c) => `<option value="${c.id}">${esc(c.name)}</option>`).join("")}</select></label><label>Location<input name="location"></label><label>Job type<select name="jobType">${["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP"].map((x) => `<option>${x}</option>`).join("")}</select></label><label>Work mode<select name="workMode">${["ONSITE", "REMOTE", "HYBRID"].map((x) => `<option>${x}</option>`).join("")}</select></label><label>Experience required (years)<input name="experienceRequired" type="number" min="0" value="0"></label><label>Minimum salary<input name="salaryMin" type="number" min="0"></label><label>Maximum salary<input name="salaryMax" type="number" min="0"></label><label>Application deadline<input name="deadline" type="date"></label><label class="wide">Description<textarea name="description" rows="4" required></textarea></label><fieldset class="wide"><legend>Required skills</legend><div class="check-grid">${skills.map((s) => `<label class="check"><input type="checkbox" name="skillIds" value="${s.id}"> ${esc(s.name)}</label>`).join("")}</div></fieldset><div class="wide"><button class="btn btn-primary">Publish job</button></div></form>`
                    : `<div class="callout warning">Create a company profile first in My companies before posting a job.</div>`,
            ) +
            card(
                "Your job postings",
                "Manage published roles",
                mine.length
                    ? `<div class="table-wrap"><table><thead><tr><th>Role</th><th>Company</th><th>Applicants</th><th>Status</th><th>Action</th></tr></thead><tbody>${mine.map((j) => `<tr><td>${esc(j.title)}</td><td>${esc(j.company)}</td><td>${j.applications}</td><td>${statusPill(j.status)}</td><td><button class="btn btn-small btn-outline" data-edit-job="${j.id}">Edit</button> <button class="btn btn-small btn-outline" data-close="${j.id}">${j.status === "OPEN" ? "Close" : "Closed"}</button></td></tr>`).join("")}</tbody></table></div>`
                    : empty("No job postings yet"),
            );
        const form = document.getElementById("jobForm");
        if (form)
            form.onsubmit = async (e) => {
                e.preventDefault();
                const f = new FormData(form);
                const obj = Object.fromEntries(f.entries());
                obj.companyId = Number(obj.companyId);
                obj.experienceRequired = Number(obj.experienceRequired);
                obj.salaryMin = obj.salaryMin ? Number(obj.salaryMin) : null;
                obj.salaryMax = obj.salaryMax ? Number(obj.salaryMax) : null;
                obj.skillIds = f.getAll("skillIds").map(Number);
                if (!obj.skillIds.length) {
                    notice("Select at least one required skill", "error");
                    return;
                }
                await api("/jobs", { method: "POST", body: JSON.stringify(obj) });
                notice("Job published");
                renderRecruiter("jobs");
            };
        view.querySelectorAll("[data-close]").forEach(
            (b) =>
            (b.onclick = async () => {
                if (b.textContent === "Closed") return;
                const id = b.dataset.close;
                await api("/jobs/" + id, { method: "DELETE" });
                notice("Job closed");
                renderRecruiter("jobs");
            }),
        );
        view.querySelectorAll("[data-edit-job]").forEach(
            (b) =>
            (b.onclick = async () => {
                const id = b.dataset.editJob;
                const j = await api("/jobs/" + id);
                const companies = await api("/companies/mine");
                const skills = await api("/skills");
                const title = prompt("Job title:", j.title);
                if (title === null) return;
                const description = prompt("Job description:", j.description);
                if (description === null) return;
                const location = prompt("Job location:", j.location || "");
                if (location === null) return;
                const company = companies.find((c) => c.name === j.company);
                const skillIds = skills
                    .filter((s) => (j.requiredSkills || []).includes(s.name))
                    .map((s) => s.id);
                await api("/jobs/" + id, {
                    method: "PUT",
                    body: JSON.stringify({
                        title,
                        description,
                        location,
                        companyId: company?.id,
                        salaryMin: j.salaryMin,
                        salaryMax: j.salaryMax,
                        experienceRequired: j.experienceRequired,
                        jobType: j.jobType,
                        workMode: j.workMode,
                        deadline: j.deadline,
                        skillIds,
                    }),
                });
                notice("Job updated");
                renderRecruiter("jobs");
            }),
        );
        return;
    }
    if (section === "applications") {
        const a = await api("/recruiter/applications");
        view.innerHTML = card(
            "Applicants",
            "Review candidates and update application status",
            a.length
                ? `<div class="table-wrap"><table><thead><tr><th>Candidate</th><th>Job</th><th>Applied</th><th>Resume</th><th>Status</th><th>Update</th></tr></thead><tbody>${a.map((x) => `<tr><td><b>${esc(x.candidate)}</b><small>${esc(x.email)}</small></td><td>${esc(x.jobTitle)}</td><td>${esc((x.appliedAt || "").slice(0, 10))}</td><td>${x.resumeUrl ? `<button type="button"
			        class="btn btn-small btn-outline"
			        data-resume-url="${esc(x.resumeUrl)}">
			    View resume
			</button>` : "—"}</td><td>${statusPill(x.status)}</td><td><select data-status="${x.id}">${["APPLIED", "UNDER_REVIEW", "SHORTLISTED", "REJECTED", "HIRED"].map((s) => `<option ${s === x.status ? "selected" : ""}>${s}</option>`).join("")}</select><button class="btn btn-small btn-primary" data-update="${x.id}">Save</button></td></tr>`).join("")}</tbody></table></div>`
                : empty("No applications received"),
        );
        // Update application status
        view.querySelectorAll("[data-update]").forEach(b => {
            b.onclick = async () => {
                const id = b.dataset.update;

                const status = view.querySelector(
                    `[data-status="${id}"]`
                ).value;

                try {
                    await api("/applications/" + id + "/status", {
                        method: "PATCH",
                        body: JSON.stringify({ status })
                    });

                    notice("Application status updated");

                } catch (e) {
                    notice(e.message, "error");
                }
            };
        });

        // View candidate resume using JWT
        view.querySelectorAll("[data-resume-url]").forEach(button => {
            button.onclick = () => {
                openProtectedResume(button.dataset.resumeUrl);
            };
        });

        return;
    }
}

async function renderAdmin(section) {
    if (section === "overview") {
        const d = await api("/admin/dashboard");
        view.innerHTML = `<div class="metrics">${metric("Total users", d.users, "Registered accounts")}${metric("Candidates", d.candidates, "Job seekers")}${metric("Recruiters", d.recruiters, "Hiring accounts")}${metric("Companies", d.companies, "Organizations")}</div><div class="metrics">${metric("Total jobs", d.jobs, "All postings")}${metric("Open jobs", d.openJobs, "Active postings")}${metric("Applications", d.applications, "Submitted applications")}${metric("Skills", d.skills, "Skill catalogue")}</div>${card("Platform administration", "Use the menu to manage accounts, job postings and skills", `<div class="callout">Admin access is restricted. Candidate and recruiter accounts cannot grant themselves administrator privileges.</div>`)}`;
        return;
    }
    if (section === "users") {
        const u = await api("/admin/users");
        view.innerHTML = card(
            "Registered users",
            "Activate or deactivate candidate and recruiter accounts",
            u.length
                ? `<div class="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Created</th><th>Status</th><th>Action</th></tr></thead><tbody>${u.map((x) => `<tr><td>${esc(x.fullName)}</td><td>${esc(x.email)}</td><td>${esc(x.role)}</td><td>${esc((x.createdAt || "").slice(0, 10))}</td><td>${x.active ? statusPill("ACTIVE") : statusPill("INACTIVE")}</td><td>${x.role === "ADMIN" ? "Protected" : `<button class="btn btn-small btn-outline" data-active="${x.id}" data-value="${!x.active}">${x.active ? "Deactivate" : "Activate"}</button>`}</td></tr>`).join("")}</tbody></table></div>`
                : empty("No users"),
        );
        view.querySelectorAll("[data-active]").forEach(
            (b) =>
            (b.onclick = async () => {
                await api("/admin/users/" + b.dataset.active + "/active", {
                    method: "PATCH",
                    body: JSON.stringify({ active: b.dataset.value === "true" }),
                });
                notice("User updated");
                renderAdmin("users");
            }),
        );
        return;
    }
    if (section === "companies") {
        const c = await api("/admin/companies");
        view.innerHTML = card(
            "Company directory",
            "Companies registered by recruiters",
            c.length
                ? `<div class="table-wrap"><table><thead><tr><th>Company</th><th>Location</th><th>Owner</th><th>Email</th></tr></thead><tbody>${c.map((x) => `<tr><td>${esc(x.name)}</td><td>${esc(x.location)}</td><td>${esc(x.owner)}</td><td>${esc(x.email)}</td></tr>`).join("")}</tbody></table></div>`
                : empty("No companies"),
        );
        return;
    }
    if (section === "applications") {
        const a = await api("/admin/applications");
        view.innerHTML = card(
            "All applications",
            "Platform-wide application activity",
            a.length
                ? `<div class="table-wrap"><table><thead><tr><th>Candidate</th><th>Email</th><th>Job</th><th>Company</th><th>Status</th></tr></thead><tbody>${a.map((x) => `<tr><td>${esc(x.candidate)}</td><td>${esc(x.email)}</td><td>${esc(x.job)}</td><td>${esc(x.company)}</td><td>${statusPill(x.status)}</td></tr>`).join("")}</tbody></table></div>`
                : empty("No applications"),
        );
        return;
    }
    if (section === "jobs") {
        const j = await api("/admin/jobs");
        view.innerHTML = card(
            "Job moderation",
            "Close or reopen any job posting",
            j.length
                ? `<div class="table-wrap"><table><thead><tr><th>Job</th><th>Company</th><th>Location</th><th>Status</th><th>Moderation</th></tr></thead><tbody>${j.map((x) => `<tr><td>${esc(x.title)}</td><td>${esc(x.company)}</td><td>${esc(x.location)}</td><td>${statusPill(x.status)}</td><td><select data-jobstatus="${x.id}">${["OPEN", "CLOSED", "EXPIRED", "DRAFT"].map((s) => `<option ${s === x.status ? "selected" : ""}>${s}</option>`).join("")}</select><button class="btn btn-small btn-primary" data-jobupdate="${x.id}">Save</button></td></tr>`).join("")}</tbody></table></div>`
                : empty("No job postings"),
        );
        view.querySelectorAll("[data-jobupdate]").forEach(
            (b) =>
            (b.onclick = async () => {
                const id = b.dataset.jobupdate,
                    s = view.querySelector(`[data-jobstatus="${id}"]`).value;
                await api("/admin/jobs/" + id + "/status", {
                    method: "PATCH",
                    body: JSON.stringify({ status: s }),
                });
                notice("Job moderated");
                renderAdmin("jobs");
            }),
        );
        return;
    }
    if (section === "skills") {
        const s = await api("/admin/skills");
        view.innerHTML = card(
            "Skill catalogue",
            "Maintain skills available to candidates and recruiters",
            `<form id="skillForm" class="inline-form"><input name="name" required placeholder="Add a skill e.g. Kubernetes"><button class="btn btn-primary">Add skill</button></form><div class="skill-admin-list">${s.map((x) => `<div class="skill-admin-item"><span>${esc(x.name)}</span><button class="btn btn-small btn-outline" data-del-skill="${x.id}">Delete</button></div>`).join("")}</div>`,
        );
        document.getElementById("skillForm").onsubmit = async (e) => {
            e.preventDefault();
            await api("/admin/skills", {
                method: "POST",
                body: JSON.stringify({ name: new FormData(e.target).get("name") }),
            });
            notice("Skill added");
            renderAdmin("skills");
        };
        view.querySelectorAll("[data-del-skill]").forEach(
            (b) =>
            (b.onclick = async () => {
                if (
                    !confirm("Delete this skill? It may be in use by profiles or jobs.")
                )
                    return;
                try {
                    await api("/admin/skills/" + b.dataset.delSkill, {
                        method: "DELETE",
                    });
                    renderAdmin("skills");
                } catch (e) {
                    notice(e.message, "error");
                }
            }),
        );
        return;
    }
}
