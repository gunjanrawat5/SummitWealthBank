import { useState } from "react";

export default function OpenAccount() {
    const [username, setUsername] = useState("");
    const [type, setType] = useState("CHECKING");
    const [initialDeposit, setInitialDeposit] = useState("");
    const [response, setResponse] = useState(null);
    const [error, setError] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const payload = {
            username: username,
            type: type,
            initialDeposit: parseFloat(initialDeposit)
        };

        try {
            const res = await fetch("http://localhost:8080/api/accounts/open", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (!res.ok) {
                const msg = await res.text();
                throw new Error(msg || `Error: ${res.status}`);
            }

            const data = await res.json();
            setResponse(data);
            setError(null);
        } catch (err) {
            setError(err.message || "Something went wrong");
            setResponse(null);
        }
    };

    const isFormValid =
        username.trim() !== "" &&
        initialDeposit.trim() !== "" &&
        !isNaN(initialDeposit);

    return (
        <div className="max-w-md mx-auto p-6 bg-white rounded-xl shadow-md space-y-4">
            <h2 className="text-xl font-semibold">Open New Account</h2>

            <form onSubmit={handleSubmit} className="space-y-3">
                <div>
                    <label>Username</label>
                    <input
                        type="text"
                        className="w-full border px-3 py-2 rounded"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>

                <div>
                    <label>Account Type</label>
                    <select
                        className="w-full border px-3 py-2 rounded"
                        value={type}
                        onChange={(e) => setType(e.target.value)}
                    >
                        <option value="CHECKING">CHECKING</option>
                        <option value="SAVINGS">SAVINGS</option>
                    </select>
                </div>

                <div>
                    <label>Initial Deposit</label>
                    <input
                        type="number"
                        className="w-full border px-3 py-2 rounded"
                        value={initialDeposit}
                        onChange={(e) => setInitialDeposit(e.target.value)}
                        required
                    />
                </div>

                <button
                    type="submit"
                    disabled={!isFormValid}
                    className={`w-full py-2 rounded text-white ${
                        isFormValid
                            ? "bg-blue-600 hover:bg-blue-700"
                            : "bg-gray-400 cursor-not-allowed"
                    }`}
                >
                    Open Account
                </button>
            </form>

            {error && (
                <div className="text-red-600 mt-4">
                    {error}
                </div>
            )}

            {response && (
                <pre className="bg-gray-100 p-4 rounded mt-4">
                    {JSON.stringify(response, null, 2)}
                </pre>
            )}
        </div>
    );
}