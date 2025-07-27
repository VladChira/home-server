import React from "react";

import Navbar from "@/components/Navbar";
import { SidebarInset, SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
import { Separator } from "@/components/ui/separator";
import { Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator } from "@/components/ui/breadcrumb";
import AppSidebar from "@/components/AppSidebar";
import { ThemeToggle } from "@/components/ThemeToggler";
import MyBreadcrumb from "@/components/MyBreadcrumb";

const MainLayout = ({ children }: { children: React.ReactNode }) => {
    return (
        <SidebarProvider>
            <AppSidebar />
            <SidebarInset >
                <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">

                    <div className="flex items-center gap-2 px-4">
                        <SidebarTrigger className="-ml-1" />
                        <Separator orientation="vertical" className="mr-2 h-4" />
                        <MyBreadcrumb></MyBreadcrumb>
                    </div>
                </header>
                <div className="flex flex-1 flex-col gap-4 p-5 pt-2">
                    {children}
                </div>
            </SidebarInset>
            <div className="absolute bottom-5 right-0 text-white">
                <ThemeToggle />
            </div>
        </SidebarProvider>
    );
};

export default MainLayout;
