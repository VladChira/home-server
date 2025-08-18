import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import React from "react"
import { onboardNewService, Service } from "@/lib/catalog"

type ServiceOnboardFormProps = {
  onSuccess?: () => void;
} & React.ComponentProps<"div">;

export function ServiceOnboardForm({
  className,
  onSuccess,
  ...props
}: ServiceOnboardFormProps) {

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();

    const form = e.currentTarget;
    const formData = new FormData(form);

    const tagsRaw = formData.get("tags") as string;
    const tags = tagsRaw.split(",").map(t => t.trim()).filter(Boolean);

    const service: Service = {
      key: formData.get("key") as string,
      service_name: formData.get("name") as string,
      owner: formData.get("owner") as string,
      description: formData.get("description") as string,
      tags: tags,
      ip: formData.get("ip") as string,
      port: Number(formData.get("port")?.toString()),
      domain: formData.get("domain") as string,
      notes: formData.get("notes") as string,
      created_at: new Date().toISOString().split("T")[0]
    };
    console.log(service);
    try {
      onboardNewService(service);
      form.reset();
      onSuccess?.();
    } catch (err) {
      console.error(err);
    }
  }

  return (
    <div className={cn("flex flex-col gap-4", className)} {...props}>
      <form onSubmit={handleSubmit}>
        <div className="flex flex-col gap-4">
          <div className="grid gap-2">
            <Label htmlFor="key">Service key (lowercase, no symbols) *</Label>
            <Input id="key" name="key" placeholder="newservicename" required />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="name">Service name *</Label>
            <Input id="name" name="name" placeholder="My new service" required />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="owner">Owner *</Label>
            <Input id="owner" name="owner" required placeholder="Vlad" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="description">Description *</Label>
            <Input id="description" name="description" required placeholder="A strategic methodology focused on fostering meaningful interactions." />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="tags">Tags *</Label>
            <Input id="tags" name="tags" required />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="ip">IP Address</Label>
            <Input id="ip" name="ip" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="port">Port</Label>
            <Input id="port" name="port" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="domain">Domain</Label>
            <Input id="domain" name="domain" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="notes">Notes</Label>
            <Input id="notes" name="notes" />
          </div>

          <div className="flex flex-col gap-3">
            <Button type="submit" className="w-full">
              Submit
            </Button>
          </div>
        </div>
      </form>
    </div>
  )
}
