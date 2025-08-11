'use client';

import ServiceCatalog from "@/components/services/ServiceCatalog";

import { Button } from "@/components/ui/button";
import { getServices, Service, useCatalogServices } from "@/lib/catalog";
import { useState, useEffect } from "react";

export default function ServicesPage() {
    const { data: services = [], isLoading, error } = useCatalogServices();
    if (isLoading) return <p>Loading…</p>;
    if (error) return <p>Failed to load VMs</p>;
    return (
        <>
            <div className="flex items-center justify-between pr-6 pt-2">
                <h1 className="text-2xl font-bold">Service Catalog</h1>
                <span><Button variant="outline">Onboard a new service</Button></span>
            </div>

            <ServiceCatalog services={services} />
        </>
    );
}
