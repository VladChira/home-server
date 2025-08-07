"use client";

import { useState, useEffect } from "react";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { ArrowRight, ExternalLink, Scan } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "../ui/button";
import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { toast } from "sonner"
import { Toaster } from "../ui/sonner";
import { deleteDNSForServiceName, provisionDNSForServiceName, Service } from "@/lib/catalog";

interface ServiceListProp {
  services: Service[];
}

const ServiceCatalog = ({ services }: ServiceListProp) => {
  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [selectedServiceForDNS, setSelectedServiceForDNS] = useState<Service | null>(null);
  const [draftDomain, setDraftDomain] = useState<string>("");

  useEffect(() => {
    if (selectedServiceForDNS) {
      setDraftDomain(selectedServiceForDNS.domain ?? "");
    }
  }, [selectedServiceForDNS]);

  return (
    <>  
      <Toaster position="top-right" />
      <main className="space-y-4 pb-6 px-6">
        {services.map((svc) => (
          <Card key={svc.key} className="w-full">
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <span>{svc.service_name}</span>
              </CardTitle>
              <CardDescription>{svc.description}</CardDescription>

              {/* Domain display + access button */}
              {svc.domain && (
                <div className="mt-2 flex items-center space-x-2">
                  <span className="text-sm text-muted-foreground">
                    http://{svc.domain}
                  </span>
                  <Button size="icon" variant="ghost" asChild aria-label={`Open ${svc.domain}`}>
                    <a href={`http://${svc.domain}`} target="_blank" rel="noopener noreferrer">
                      <ExternalLink className="h-4 w-4" />
                    </a>
                  </Button>
                </div>
              )}
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-2">
                {svc.tags.map((tag) => (
                  <Badge key={tag} variant={tag === "core" ? "destructive" : "secondary"}>
                    {tag}
                  </Badge>
                ))}
              </div>
            </CardContent>
            <CardFooter className="flex justify-end gap-2">
              <Button
                size="sm"
                variant="outline"
                className="flex items-center"
                onClick={() => setSelectedServiceForDNS(svc)}
              >
                DNS Configuration
                <Scan className="ml-2 h-4 w-4" />
              </Button>
              <Button
                size="sm"
                variant="outline"
                className="flex items-center"
                onClick={() => setSelectedService(svc)}
              >
                View details
                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </CardFooter>
          </Card>
        ))}

        {/* Details Dialog */}
        <Dialog open={selectedService !== null} onOpenChange={(open) => !open && setSelectedService(null)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{selectedService?.service_name}</DialogTitle>
              <DialogDescription>Detailed information for this service.</DialogDescription>
            </DialogHeader>

            <div className="space-y-2 pt-4">
              {selectedService && (
                <ul className="space-y-1">
                  {Object.entries(selectedService).map(([key, value]) => (
                    <li key={key} className="flex">
                      <span className="w-32 font-medium capitalize">{key.replace('_', ' ')}:</span>
                      <span>
                        {value === undefined || value === null || (Array.isArray(value) && value.length === 0)
                          ? 'N/A'
                          : Array.isArray(value)
                          ? value.join(', ')
                          : value.toString()}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <DialogFooter>
              <Button onClick={() => setSelectedService(null)}>Close</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        {/* DNS Configuration Dialog */}
        <Dialog open={selectedServiceForDNS !== null && selectedService == null} onOpenChange={(open) => !open && setSelectedServiceForDNS(null)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{selectedServiceForDNS?.service_name ?? 'DNS Configuration'}</DialogTitle>
              <DialogDescription>Configure a domain name for this service.</DialogDescription>
            </DialogHeader>
            {/* Existing domain display */}
            <div>
              <span className="font-medium">Current domain name:</span>{' '}
              {selectedServiceForDNS?.domain ? (
                <code>{selectedServiceForDNS.domain}</code>
              ) : (
                <span className="text-muted-foreground">This service does not have a domain configured.</span>
              )}
            </div>

            {/* Input for new domain */}
            <div className="space-y-1 mt-4">
              <Label htmlFor="new-domain">New domain:</Label>
              <Input
                id="new-domain"
                placeholder={`${selectedServiceForDNS?.key}.server.local`}
                value={draftDomain}
                onChange={(e) => setDraftDomain(e.target.value)}
              />
            </div>

            <DialogFooter>
              <Button
                onClick={async () => {
                  try {
                    await provisionDNSForServiceName(selectedServiceForDNS!.key, draftDomain);
                    toast.success('Domain configured');
                  } catch (err: any) {
                    toast.error(err.message || 'Failed to configure domain');
                  } finally {
                    setSelectedServiceForDNS(null);
                  }
                }}
              >
                Save
              </Button>
              <Button
                variant="destructive"
                onClick={async () => {
                  try {
                    await deleteDNSForServiceName(selectedServiceForDNS!.key);
                    toast.success('Domain removed');
                  } catch (err: any) {
                    toast.error(err.message || 'Failed to remove domain');
                  } finally {
                    setSelectedServiceForDNS(null);
                  }
                }}
              >
                Remove
              </Button>
              <Button variant="ghost" onClick={() => setSelectedServiceForDNS(null)}>
                Cancel
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </main>
    </>
  );
};

export default ServiceCatalog;
