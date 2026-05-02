// ================================================
// API REST con Firebase Cloud Functions + TypeScript
// ================================================

import * as functions from "firebase-functions"; // ← Para exportar la función HTTPS
import * as admin from "firebase-admin";
import express from "express";
import cors from "cors";

// Inicializamos Firebase Admin SDK
admin.initializeApp();
const db = admin.firestore();

// Creamos la aplicación Express
const app = express();
app.use(cors({origin: true})); // Permite peticiones desde tu app Android

// ================================================
// ENDPOINT 1: Registrar un usuario
// ================================================
app.post("/api/register", async (req: express.Request, res: express.Response) => {
  try {
    const {name, email, age} = req.body;

    if (!name || !email || !age) {
      res.status(400).json({error: "Faltan datos: name, email y age son obligatorios"});
      return;
    }

    const userRef = db.collection("usuarios").doc();
    await userRef.set({
      nombre: name,
      email: email,
      edad: Number(age),
      fechaRegistro: Date.now(),
    });

    res.status(201).json({
      success: true,
      message: "Usuario registrado correctamente",
      userId: userRef.id,
    });
  } catch (error: any) {
    res.status(500).json({error: error.message});
  }
});

// ================================================
// ENDPOINT 2: Obtener todos los usuarios
// ================================================
app.get("/api/users", async (req: express.Request, res: express.Response) => {
  try {
    const snapshot = await db.collection("usuarios").get();
    const users: any[] = [];

    snapshot.forEach((doc) => {
      users.push({id: doc.id, ...doc.data()});
    });

    res.status(200).json(users);
  } catch (error: any) {
    res.status(500).json({error: error.message});
  }
});

// ================================================
// ENDPOINT 3: Obtener un usuario por ID
// ================================================
app.get("/api/user/:uid", async (req: express.Request, res: express.Response) => {
  try {
    const uid = Array.isArray(req.params.uid) ? req.params.uid[0] : req.params.uid;

    if (!uid) {
      res.status(400).json({error: "Se requiere el ID del usuario"});
      return;
    }

    const doc = await db.collection("usuarios").doc(uid).get();

    if (!doc.exists) {
      res.status(404).json({error: "Usuario no encontrado"});
      return;
    }

    res.status(200).json({
      id: doc.id,
      ...doc.data(),
    });
  } catch (error: any) {
    res.status(500).json({error: error.message});
  }
});

// Exportamos la API como Cloud Function (esto es lo que se despliega)
export const api = functions.https.onRequest(app);

console.log("API REST de Firebase inicializada correctamente");

