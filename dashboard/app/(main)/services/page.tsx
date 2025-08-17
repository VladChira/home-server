'use client';

import ServiceCatalog from "@/components/services/ServiceCatalog";
import { ServiceOnboardForm } from "@/components/services/ServiceOnboardForm";

import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { getServices, Service, useCatalogServices } from "@/lib/catalog";
import { useState, useEffect } from "react";

export default function ServicesPage() {
    const [onboardNew, setOnboardDomain] = useState<boolean>(false);


    const { data: services = [], isLoading, error } = useCatalogServices();
    if (isLoading) return <p>Loading…</p>;
    if (error) return <p>Failed to load VMs</p>;


    return (
        <>
            <div className="flex items-center justify-between pr-6 pt-2">
                <h1 className="text-2xl font-bold">Service Catalog</h1>
                <span><Button variant="outline" onClick={() => setOnboardDomain(true)}>Onboard a new service</Button></span>
            </div>

            <ServiceCatalog services={services} />

            <Dialog open={onboardNew} onOpenChange={(open) => !open && setOnboardDomain(open)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Onboard an existing service to the catalog</DialogTitle>
                        <DialogDescription>Fill in the details of the new service.</DialogDescription>
                    </DialogHeader>

                    <ServiceOnboardForm />
                </DialogContent>
            </Dialog>
        </>
    );
}
