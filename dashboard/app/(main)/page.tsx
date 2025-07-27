import DashboardCard from "@/components/dashboard/DashboardCard";

export default function Home() {
  return (
    <>
      <div className="flex flex-col md:flex-row justify-between gap-5 mb">
        <DashboardCard />
      </div>
    </>
  );
}
