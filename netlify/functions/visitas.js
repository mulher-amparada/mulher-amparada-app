import { MongoClient } from "mongodb";

const uri = process.env.MONGO_URI;

let client;
let clientPromise;

if (!global._mongoClientPromise) {
  client = new MongoClient(uri);
  global._mongoClientPromise = client.connect();
}

clientPromise = global._mongoClientPromise;

export async function handler(event) {
  try {
    const client = await clientPromise;

    const db = client.db("meusite");
    const collection = db.collection("contador");

    let doc = await collection.findOne({ name: "visitas" });

    if (!doc) {
      await collection.insertOne({
        name: "visitas",
        value: 0
      });
    }

    if (event.httpMethod === "GET") {
      const atualizado = await collection.findOne({
        name: "visitas"
      });

      return {
        statusCode: 200,
        headers: {
          "Access-Control-Allow-Origin": "*"
        },
        body: JSON.stringify({
          visitas: atualizado.value
        })
      };
    }

    if (event.httpMethod === "POST") {
      const source =
        event.headers["x-source"] || "";

      if (source === "github") {
        await collection.updateOne(
          { name: "visitas" },
          { $inc: { value: 1 } }
        );
      }

      const atualizado = await collection.findOne({
        name: "visitas"
      });

      return {
        statusCode: 200,
        headers: {
          "Access-Control-Allow-Origin": "*"
        },
        body: JSON.stringify({
          visitas: atualizado.value
        })
      };
    }

    return {
      statusCode: 405,
      body: JSON.stringify({
        erro: "Método não permitido"
      })
    };

  } catch (error) {
    return {
      statusCode: 500,
      body: JSON.stringify({
        erro: error.message
      })
    };
  }
}