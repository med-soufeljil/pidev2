<?php

namespace App\Entity;

use App\Repository\ReunionRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ReunionRepository::class)]
#[ORM\Table(name: 'reunion')]
class Reunion
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idReunion')]
    private ?int $id = null;

    #[ORM\Column(name: 'idRH')]
    private int $idRh = 0;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(name: 'idCandidat', referencedColumnName: 'idCandidat', nullable: false)]
    private ?Candidat $candidat = null;

    #[ORM\Column(name: 'date', type: 'datetime_immutable')]
    private ?\DateTimeImmutable $date = null;

    #[ORM\Column(name: 'link', length: 255)]
    private string $link = '';

    public function getId(): ?int { return $this->id; }
    public function getIdRh(): int { return $this->idRh; }
    public function setIdRh(int $idRh): self { $this->idRh = $idRh; return $this; }
    public function getCandidat(): ?Candidat { return $this->candidat; }
    public function setCandidat(?Candidat $candidat): self { $this->candidat = $candidat; return $this; }
    public function getDate(): ?\DateTimeImmutable { return $this->date; }
    public function setDate(?\DateTimeImmutable $date): self { $this->date = $date; return $this; }
    public function getLink(): string { return $this->link; }
    public function setLink(string $link): self { $this->link = $link; return $this; }
}
