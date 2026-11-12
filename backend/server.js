
require("dotenv").config();

const express = require("express");
const helmet = require("helmet");
const cors = require("cors");
const rateLimit = require("express-rate-limit");

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: "1mb" }));

app.use("/api/", rateLimit({
    windowMs: 60 * 1000,
    limit: 20,
    standardHeaders: "draft-7",
    legacyHeaders: false
}));

app.get("/health", (req, res) => {
    res.json({
        status: "ok",
        app: "HEX-AI",
        version: "1.0.0"
    });
});

app.post("/api/chat", async (req, res) => {
    try {
        const apiKey = process.env.OPENROUTER_API_KEY;
        const model = process.env.OPENROUTER_MODEL || "openrouter/free";

        if (!apiKey) {
            return res.status(500).json({
                error: "La clé API n'est pas configurée sur le serveur."
            });
        }

        const incomingMessages = req.body.messages;

        if (!Array.isArray(incomingMessages) || incomingMessages.length === 0) {
            return res.status(400).json({
                error: "Messages manquants."
            });
        }

        if (incomingMessages.length > 20) {
            return res.status(400).json({
                error: "Conversation trop longue."
            });
        }

        const messages = incomingMessages.map(message => {
            if (
                !["user", "assistant", "system"].includes(message.role) ||
                typeof message.content !== "string"
            ) {
                throw new Error("Format de message invalide.");
            }

            return {
                role: message.role,
                content: message.content.slice(0, 12000)
            };
        });

        const upstream = await fetch(
            "https://openrouter.ai/api/v1/chat/completions",
            {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${apiKey}`,
                    "Content-Type": "application/json",
                    "HTTP-Referer": process.env.APP_URL || "https://central-hex.example",
                    "X-Title": "HEX-AI - CENTRAL-HEX"
                },
                body: JSON.stringify({
                    model,
                    messages
                })
            }
        );

        const data = await upstream.json();

        if (!upstream.ok) {
            console.error("OpenRouter error:", upstream.status, data);

            return res.status(502).json({
                error: "Le fournisseur IA n'a pas pu traiter la demande."
            });
        }

        const reply = data?.choices?.[0]?.message?.content;

        if (typeof reply !== "string" || !reply.trim()) {
            return res.status(502).json({
                error: "Réponse IA vide ou invalide."
            });
        }

        return res.json({ reply });

    } catch (error) {
        console.error("HEX-AI error:", error.message);

        return res.status(500).json({
            error: "Erreur interne du serveur."
        });
    }
});

const port = process.env.PORT || 3000;

app.listen(port, "0.0.0.0", () => {
    console.log(`HEX-AI backend démarré sur le port ${port}`);
});
