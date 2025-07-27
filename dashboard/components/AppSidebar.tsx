import { LayoutDashboard, Server, Images, TvMinimalPlay, Box, User, MonitorCog, PackageOpen } from "lucide-react";

import Link from "next/link";

import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarGroup,
    SidebarGroupContent,
    SidebarGroupLabel,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarRail,
} from "@/components/ui/sidebar"
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";

const JELLYFIN_URL = process.env.NEXT_PUBLIC_JELLYFIN_URL

const AppSidebar = () => {
    return (
        <Sidebar collapsible="icon">
            <SidebarHeader>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <SidebarMenuButton
                            size="lg"
                            className="data-[state=open]:bg-sidebar-accent data-[state=open]:text-sidebar-accent-foreground"
                        >
                            <div className="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
                                <MonitorCog className="size-4" />
                            </div>
                            <div className="grid flex-1 text-left text-sm leading-tight">
                                <span className="truncate font-semibold">
                                    C9 Server Manager
                                </span>
                                <span className="truncate text-xs">v0.01</span>
                            </div>
                        </SidebarMenuButton>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarHeader>
            <SidebarContent className="bg-sidebar-background">
                {/* Internal Services Section */}
                <SidebarGroup >
                    <SidebarGroupLabel>Internal Services</SidebarGroupLabel>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            <SidebarMenuItem key="dashboard">
                                <SidebarMenuButton asChild>
                                    <Link href="/">
                                        <LayoutDashboard size={128} className="mr-2" />
                                        <span>Dashboard</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                            <SidebarMenuItem key="virtual-machines">
                                <SidebarMenuButton asChild>
                                    <Link href="/virtual-machines">
                                        <Server className="mr-2 h-4 w-4" />
                                        <span>Virtual Machines</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                            <SidebarMenuItem key="minecraft">
                                <SidebarMenuButton asChild>
                                    <Link href="/minecraft">
                                        <Box className="mr-2 h-4 w-4" />
                                        <span>Minecraft Servers</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                            <SidebarMenuItem key="services">
                                <SidebarMenuButton asChild>
                                    <Link href="/services">
                                        <PackageOpen className="mr-2 h-4 w-4" />
                                        <span>Service Catalog</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>

                {/* External Services Section */}
                <SidebarGroup>
                    <SidebarGroupLabel>External Services</SidebarGroupLabel>
                    <SidebarGroupContent>
                        <SidebarMenu>
                            <SidebarMenuItem key="jellyfin">
                                <SidebarMenuButton asChild>
                                    <Link href={`${JELLYFIN_URL}`}>
                                        <TvMinimalPlay className="mr-2 h-4 w-4" />
                                        <span>Jellyfin Media Player</span>
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>
            </SidebarContent>
            <SidebarFooter>
                <div className="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
                    <Avatar className="h-8 w-8 rounded-lg">
                        <AvatarImage src="https://github.com/shadcn.png" alt="Avatar Logo" />
                        <AvatarFallback className="rounded-lg">CN</AvatarFallback>
                    </Avatar>
                    <div className="grid flex-1 text-left text-sm leading-tight">
                        <span className="truncate font-semibold">User</span>
                        <span className="truncate text-xs">TODO</span>
                    </div>
                </div>
            </SidebarFooter>
            <SidebarRail />
        </Sidebar>
    );
};

export default AppSidebar;
