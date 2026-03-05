<?php

namespace App\Entity;

use App\Repository\OffreRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: OffreRepository::class)]
#[ORM\Table(name: 'offre')]
class Offre
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idOffre')]
    private ?int $id = null;

    #[ORM\Column(name: 'nomOffre', length: 255)]
    private string $nomOffre = '';

    #[ORM\Column(name: 'type', enumType: TypeOffre::class)]
    private TypeOffre $type = TypeOffre::CDI;

    #[ORM\Column(name: 'competences', type: 'text')]
    private string $competences = '';

    #[ORM\Column(name: 'salaire')]
    private int $salaire = 0;

    public function getId(): ?int { return $this->id; }
    public function getNomOffre(): string { return $this->nomOffre; }
    public function setNomOffre(string $nomOffre): self { $this->nomOffre = $nomOffre; return $this; }
    public function getType(): TypeOffre { return $this->type; }
    public function setType(TypeOffre $type): self { $this->type = $type; return $this; }
    public function getCompetences(): string { return $this->competences; }
    public function setCompetences(string $competences): self { $this->competences = $competences; return $this; }
    public function getSalaire(): int { return $this->salaire; }
    public function setSalaire(int $salaire): self { $this->salaire = $salaire; return $this; }
}
