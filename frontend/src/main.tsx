import React, { useState } from "react";
import { createRoot } from "react-dom/client";
import { api, setAuth } from "./services/api";

function App() {
  const [userId, setUserId] = useState<number | null>(null);
  const [email, setEmail] = useState("demo@aijobtracker.com");
  const [password, setPassword] = useState("password123");
  const [jobs, setJobs] = useState<any[]>([]);
  const [interviews, setInterviews] = useState<any[]>([]);
  const [summary, setSummary] = useState<Record<string, number>>({});
  const [question, setQuestion] = useState("What interviews do I have tomorrow?");
  const [answer, setAnswer] = useState("");
  const [importUrl, setImportUrl] = useState("");
  const [importResult, setImportResult] = useState<any>(null);
  const [resumeResult, setResumeResult] = useState<any>(null);

  const ensureAccount = async () => {
    try {
      const login = await api.post("/auth/login", { email, password });
      const id = login.data.userId as number;
      setUserId(id);
      setAuth(id);
    } catch {
      const signup = await api.post("/auth/signup", { email, password, fullName: "Demo User" });
      const id = signup.data.userId as number;
      setUserId(id);
      setAuth(id);
    }
  };

  const loadData = async () => {
    const [j, i, s] = await Promise.all([
      api.get("/jobs"),
      api.get("/interviews"),
      api.get("/dashboard/summary")
    ]);
    setJobs(j.data);
    setInterviews(i.data);
    setSummary(s.data);
  };

  const askAssistant = async () => {
    const res = await api.post("/assistant/chat", { message: question });
    setAnswer(res.data.answer);
  };

  const importJobUrl = async () => {
    const res = await api.post("/jobs/import-from-url", { url: importUrl });
    setImportResult(res.data);
  };

  const uploadResume = async (file: File) => {
    const form = new FormData();
    form.append("file", file);
    const res = await api.post("/resumes/upload", form, {
      headers: { "Content-Type": "multipart/form-data" }
    });
    setResumeResult(res.data);
  };

  return (
    <div style={{ maxWidth: 900, margin: "0 auto", fontFamily: "Arial, sans-serif", padding: 20 }}>
      <h1>AI Job Tracker and Assistant</h1>
      <p>Production-ready starter for job tracking and interview workflows.</p>
      {!userId ? (
        <div style={{ display: "flex", gap: 10, marginBottom: 20 }}>
          <input value={email} onChange={(e) => setEmail(e.target.value)} />
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
          <button onClick={ensureAccount}>Login / Signup</button>
        </div>
      ) : (
        <p>Logged in as user #{userId}</p>
      )}

      <div style={{ display: "flex", gap: 8, marginBottom: 20 }}>
        <button disabled={!userId} onClick={loadData}>Refresh Dashboard</button>
        <button
          disabled={!userId}
          onClick={async () => {
            await api.post("/jobs", {
              companyName: "Example Corp",
              roleTitle: "Full Stack Developer",
              platform: "LINKEDIN",
              status: "APPLIED",
              appliedDate: new Date().toISOString().slice(0, 10)
            });
            await loadData();
          }}
        >
          Add Sample Job
        </button>
      </div>

      <h2>Dashboard</h2>
      <pre>{JSON.stringify(summary, null, 2)}</pre>

      <h2>Jobs</h2>
      <pre>{JSON.stringify(jobs, null, 2)}</pre>

      <h2>Interviews</h2>
      <pre>{JSON.stringify(interviews, null, 2)}</pre>

      <h2>AI Assistant</h2>
      <div style={{ display: "flex", gap: 8, marginBottom: 10 }}>
        <button disabled={!userId} onClick={() => setQuestion("What interviews do I have tomorrow?")}>Tomorrow interviews</button>
        <button disabled={!userId} onClick={() => setQuestion("How many jobs did I apply this week?")}>Applied this week</button>
        <button disabled={!userId} onClick={() => setQuestion("Show pending applications")}>Pending applications</button>
      </div>
      <div style={{ display: "flex", gap: 8 }}>
        <input style={{ flex: 1 }} value={question} onChange={(e) => setQuestion(e.target.value)} />
        <button disabled={!userId} onClick={askAssistant}>Ask</button>
      </div>
      <p>{answer}</p>

      <h2>Import Job Link (Public metadata only)</h2>
      <div style={{ display: "flex", gap: 8 }}>
        <input style={{ flex: 1 }} value={importUrl} onChange={(e) => setImportUrl(e.target.value)} placeholder="https://..." />
        <button disabled={!userId || !importUrl} onClick={importJobUrl}>Import URL</button>
      </div>
      <pre>{importResult ? JSON.stringify(importResult, null, 2) : "No import yet."}</pre>

      <h2>Resume Analyzer</h2>
      <input
        type="file"
        accept="application/pdf"
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file && userId) uploadResume(file);
        }}
      />
      <pre>{resumeResult ? JSON.stringify(resumeResult, null, 2) : "No resume uploaded yet."}</pre>
    </div>
  );
}

createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
