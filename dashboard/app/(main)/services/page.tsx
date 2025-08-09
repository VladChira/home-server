'use client';

import ServiceCatalog from "@/components/services/ServiceCatalog";

import { Button } from "@/components/ui/button";
import { getServices, Service } from "@/lib/catalog";
import { useState, useEffect } from "react";

export default function ServicesPage() {
    const [error, setError] = useState();
    const [isLoading, setIsLoading] = useState(false);
    const [services, setServices] = useState<Service[]>([]);

    useEffect(() => {
        const fetchServices = async () => {
            setIsLoading(true);
            try {
                const services = await getServices();
                setServices(services);
            } catch (e: any) {
                setError(e);
            } finally {
                setIsLoading(false);
            }
        }
        fetchServices();
    }, [])
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
