"use client"

import * as React from "react"

import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import { login } from "@/app/(auth)/auth/actions"

import { useActionState } from "react"
import { useFormStatus } from "react-dom"

const AuthTab = () => {
    const [state, loginAction] = useActionState(login, undefined);
    return (
        <Card className="w-[350px]">
            <CardHeader>
                <CardTitle className="text-3xl">Login</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
                <form action={loginAction}>
                    <div className="grid w-full items-center gap-4">
                        <div className="flex flex-col space-y-1.5">
                            <Label htmlFor="username" className="text-xl">Username</Label>
                            <Input id="username" name="username" placeholder="Username" />
                        </div>
                        <div className="flex flex-col space-y-1.5">
                            <Label htmlFor="password" className="text-xl">Password</Label>
                            <Input id="password" placeholder="Password" name="password" type="password" />
                        </div>
                    </div>
                    <br />
                    <SubmitButton />
                </form>
            </CardContent>
            <CardFooter>
                <br/>
                {state?.errors?.username && <p className="text-red-500">{state.errors.username}</p>}
            </CardFooter>
        </Card>
    );
}

function SubmitButton() {
    const { pending } = useFormStatus();

    return (<Button disabled={pending} className="w-full">Log in</Button>);
}

export default AuthTab;