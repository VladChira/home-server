import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

export function ServiceOnboardForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  return (
    <div className={cn("flex flex-col gap-4", className)} {...props}>
      <form>
        <div className="flex flex-col gap-4">
          <div className="grid gap-2">
            <Label htmlFor="key">Service key (lowercase, no symbols) *</Label>
            <Input id="key" placeholder="newservicename" required />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="name">Service name *</Label>
            <Input id="name" placeholder="My new service" required />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="owner">Owner *</Label>
            <Input id="owner" required placeholder="Vlad" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="description">Description *</Label>
            <Input id="description" required placeholder="A strategic methodology focused on fostering meaningful interactions." />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="tags">Tags *</Label>
            <Input id="tags" required />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="ip">IP Address</Label>
            <Input id="ip" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="port">Port</Label>
            <Input id="port" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="domain">Domain</Label>
            <Input id="domain" />
          </div>

          <div className="grid gap-2">
            <Label htmlFor="notes">Notes</Label>
            <Input id="notes" />
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
