import type { Metadata } from "next";
import Link from "next/link";
import "./globals.css";

export const metadata: Metadata = {
  title: "Support Ticket Management",
  description: "Support / Jira-like ticket management UI",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <header className="app-header">
          <Link href="/" className="app-brand">
            Support Tickets
          </Link>
          <nav className="app-nav">
            <Link href="/">Tickets</Link>
            <Link href="/tickets/new">New ticket</Link>
          </nav>
        </header>
        <main className="app-main">{children}</main>
      </body>
    </html>
  );
}
