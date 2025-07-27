export const dynamic = 'force-dynamic';

import { getServices, Service } from "@/app/actions/serviceCatalogActions";
import ServiceCatalog from "@/components/services/ServiceCatalog";

import { Button } from "@/components/ui/button";

export default async function ServicesPage() {
    const services = await getServices();
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