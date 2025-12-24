"use client";

import { type FormEvent, useId, useState } from "react";
import { ZodError } from "zod";

import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { LoginSchema } from "@/schema/login-schema";
import { SignupSchema } from "@/schema/signup-schema";

export type EmailAuthPayload = {
  email: string;
  password: string;
  name?: string;
  mode: "login" | "signup";
};

type LocalAuthTabsProps = {
  loading: boolean;
  onAuth: (payload: EmailAuthPayload) => Promise<void>;
  error?: string | null;
};

/**
 * Renders login and signup tabs for local auth.
 * @param loading True when auth is in progress.
 * @param onAuth Handler for login or signup submit.
 * @param error Optional error message from backend.
 * @returns Local auth tab UI.
 * @author Maruf Bepary
 */
export const LocalAuthTabs = ({
  loading,
  onAuth,
  error,
}: LocalAuthTabsProps) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [validationErrors, setValidationErrors] = useState<
    Record<string, string>
  >({});

  const loginEmailId = useId();
  const loginPasswordId = useId();
  const signupNameId = useId();
  const signupEmailId = useId();
  const signupPasswordId = useId();

  const handleSubmit = (
    e: FormEvent<HTMLFormElement>,
    mode: EmailAuthPayload["mode"]
  ) => {
    e.preventDefault();
    setValidationErrors({});

    try {
      if (mode === "login") {
        LoginSchema.parse({ email, password });
      } else {
        SignupSchema.parse({ email, password, name });
      }
      onAuth({ email, password, name, mode });
    } catch (err) {
      if (err instanceof ZodError) {
        const { errors } = err as unknown as {
          errors: { path: (string | number)[]; message: string }[];
        };

        const newErrors: Record<string, string> = {};
        errors.forEach((error) => {
          if (error.path[0]) {
            newErrors[error.path[0] as string] = error.message;
          }
        });
        setValidationErrors(newErrors);
      }
    }
  };

  return (
    <Tabs defaultValue="login" className="w-full">
      <TabsList className="grid w-full grid-cols-2">
        <TabsTrigger value="login">Login</TabsTrigger>
        <TabsTrigger value="signup">Sign Up</TabsTrigger>
      </TabsList>

      {error && (
        <div className="mt-4 p-3 text-sm text-red-500 bg-red-50 border border-red-200 rounded-md">
          {error}
        </div>
      )}

      <TabsContent value="login">
        <form onSubmit={(e) => handleSubmit(e, "login")} className="space-y-3">
          <div className="space-y-1">
            <label
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
              htmlFor={loginEmailId}
            >
              Email
            </label>
            <input
              id={loginEmailId}
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className={`flex h-10 w-full rounded-md border bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${
                validationErrors.email
                  ? "border-red-500 focus-visible:ring-red-500"
                  : "border-input"
              }`}
              placeholder="name@example.com"
            />
            {validationErrors.email && (
              <p className="text-xs text-red-500">{validationErrors.email}</p>
            )}
          </div>
          <div className="space-y-1">
            <label
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
              htmlFor={loginPasswordId}
            >
              Password
            </label>
            <input
              id={loginPasswordId}
              type="password"
              value={password}
              placeholder="********"
              onChange={(event) => setPassword(event.target.value)}
              className={`flex h-10 w-full rounded-md border bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${
                validationErrors.password
                  ? "border-red-500 focus-visible:ring-red-500"
                  : "border-input"
              }`}
            />
            {validationErrors.password && (
              <p className="text-xs text-red-500">
                {validationErrors.password}
              </p>
            )}
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? "Processing..." : "Sign Up"}
          </Button>
        </form>
      </TabsContent>

      <TabsContent value="signup">
        <form onSubmit={(e) => handleSubmit(e, "signup")} className="space-y-3">
          <div className="space-y-1">
            <label
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
              htmlFor={signupNameId}
            >
              Name
            </label>
            <input
              id={signupNameId}
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              className={`flex h-10 w-full rounded-md border bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${
                validationErrors.name
                  ? "border-red-500 focus-visible:ring-red-500"
                  : "border-input"
              }`}
              placeholder="John Doe"
            />
            {validationErrors.name && (
              <p className="text-xs text-red-500">{validationErrors.name}</p>
            )}
          </div>
          <div className="space-y-1">
            <label
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
              htmlFor={signupEmailId}
            >
              Email
            </label>
            <input
              id={signupEmailId}
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className={`flex h-10 w-full rounded-md border bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${
                validationErrors.email
                  ? "border-red-500 focus-visible:ring-red-500"
                  : "border-input"
              }`}
              placeholder="name@example.com"
            />
            {validationErrors.email && (
              <p className="text-xs text-red-500">{validationErrors.email}</p>
            )}
          </div>
          <div className="space-y-1">
            <label
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
              htmlFor={signupPasswordId}
            >
              Password
            </label>
            <input
              id={signupPasswordId}
              type="password"
              value={password}
              placeholder="********"
              onChange={(event) => setPassword(event.target.value)}
              className={`flex h-10 w-full rounded-md border bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${
                validationErrors.password
                  ? "border-red-500 focus-visible:ring-red-500"
                  : "border-input"
              }`}
            />
            {validationErrors.password && (
              <p className="text-xs text-red-500">
                {validationErrors.password}
              </p>
            )}
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? "Processing..." : "Create Account"}
          </Button>
        </form>
      </TabsContent>
    </Tabs>
  );
};
