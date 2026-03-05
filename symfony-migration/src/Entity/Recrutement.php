<?php

namespace App\Entity;

use App\Repository\RecrutementRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: RecrutementRepository::class)]
#[ORM\Table(name: 'recrutement')]
class Recrutement
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idRec')]
    private ?int $id = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(name: 'idOffre', referencedColumnName: 'idOffre', nullable: false)]
    private ?Offre $offre = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(name: 'idCandidat', referencedColumnName: 'idCandidat', nullable: false)]
    private ?Candidat $candidat = null;

    public function getId(): ?int { return $this->id; }
    public function getOffre(): ?Offre { return $this->offre; }
    public function setOffre(?Offre $offre): self { $this->offre = $offre; return $this; }
    public function getCandidat(): ?Candidat { return $this->candidat; }
    public function setCandidat(?Candidat $candidat): self { $this->candidat = $candidat; return $this; }
}
