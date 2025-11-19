'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

const AuthTab = () => {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        const form = e.currentTarget;
        const username = (form.username as HTMLInputElement).value.trim();
        const password = (form.password as HTMLInputElement).value;

        try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_API_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password }),
            });

            if (!res.ok) {
                if (res.status === 401) {
                    setError('Invalid username or password.');
                } else {
                    setError(`Login failed (${res.status})`);
                }
                return;
            }

            const data = await res.json();
            if (!data?.accessToken) {
                setError('Login response did not include an access token.');
                return;
            }

            // Store the token in localStorage for later API calls
            localStorage.setItem('accessToken', data.accessToken);
            // lightweight mirror cookie for middleware to read
            document.cookie = `accessToken=${data.accessToken}; Path=/; SameSite=Lax`;

            router.push('/');
        } catch (err) {
            console.error(err);
            setError('Something went wrong while logging in.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Card className="w-[350px]">
            <CardHeader>
                <CardTitle className="text-3xl">Login</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
                <form onSubmit={handleSubmit}>
                    <div className="grid w-full items-center gap-4">
                        <div className="flex flex-col space-y-1.5">
                            <Label htmlFor="username" className="text-xl">Username</Label>
                            <Input id="username" name="username" placeholder="Username" />
                        </div>
                        <div className="flex flex-col space-y-1.5">
                            <Label htmlFor="password" className="text-xl">Password</Label>
                            <Input id="password" name="password" type="password" placeholder="Password" />
                        </div>
                    </div>
                    <br />
                    <Button disabled={loading} className="w-full">
                        {loading ? 'Logging in…' : 'Log in'}
                    </Button>
                </form>
            </CardContent>
            <CardFooter className="min-h-6">
                {error && <p className="text-red-500">{error}</p>}
            </CardFooter>
        </Card>
    );
};

export default AuthTab;
