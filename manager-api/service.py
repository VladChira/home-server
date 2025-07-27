from dataclasses import dataclass, asdict
from typing import List, Optional

@dataclass
class Service:
    key: str
    service_name: str
    owner: str
    description: str
    created_at: str
    tags: List[str]
    ip: Optional[str] = None
    port: Optional[int] = None
    domain: Optional[str] = None
    notes: Optional[str] = None