"use server"

import { createSession, deleteSession } from "@/lib/session";
import { redirect } from "next/navigation";

const users = [
    {
        id: "1",
        username: "vlad",
        password: "sololeveling"
    },
    {
        id: "2",
        username: "damian",
        password: "noob"
    },
    {
        id: "3",
        username: "denis",
        password: "iocla"
    },
    {
        id: "4",
        username: "victor",
        password: "vektor"
    }
]

export async function login(prevState: any, formData: FormData) {
    const { username, password } = Object.fromEntries(formData);

    const foundUser = users.find((user) => user.username === username && user.password === password);
    if (foundUser === undefined) {
        return {
            errors: {
                username: ["Invalid username or password"],
            },
        };
    }

    await createSession(foundUser.id);
    redirect("/");
}

export async function logout() {
    await deleteSession();
    redirect("/auth")
}