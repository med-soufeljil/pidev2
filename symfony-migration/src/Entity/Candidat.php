<?php

namespace App\Entity;

use App\Repository\CandidatRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CandidatRepository::class)]
class Candidat
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    private string $nom = '';

    #[ORM\Column(length: 255)]
    private string $prenom = '';

    #[ORM\Column]
    private int $cin = 0;

    #[ORM\Column]
    private int $tel = 0;

    #[ORM\Column(length: 255)]
    private string $adresse = '';

    #[ORM\Column(length: 255)]
    private string $email = '';

    #[ORM\Column(length: 255)]
    private string $cv = '';

    #[ORM\Column(length: 64)]
    private string $statut = 'Nouveau';

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $aiAnalyse = null;

    #[ORM\Column]
    private int $aiScore = 0;

    public function getId(): ?int { return $this->id; }
    public function getNom(): string { return $this->nom; }
    public function setNom(string $nom): self { $this->nom = $nom; return $this; }
    public function getPrenom(): string { return $this->prenom; }
    public function setPrenom(string $prenom): self { $this->prenom = $prenom; return $this; }
    public function getCin(): int { return $this->cin; }
    public function setCin(int $cin): self { $this->cin = $cin; return $this; }
    public function getTel(): int { return $this->tel; }
    public function setTel(int $tel): self { $this->tel = $tel; return $this; }
    public function getAdresse(): string { return $this->adresse; }
    public function setAdresse(string $adresse): self { $this->adresse = $adresse; return $this; }
    public function getEmail(): string { return $this->email; }
    public function setEmail(string $email): self { $this->email = $email; return $this; }
    public function getCv(): string { return $this->cv; }
    public function setCv(string $cv): self { $this->cv = $cv; return $this; }
    public function getStatut(): string { return $this->statut; }
    public function setStatut(string $statut): self { $this->statut = $statut; return $this; }
    public function getAiAnalyse(): ?string { return $this->aiAnalyse; }
    public function setAiAnalyse(?string $aiAnalyse): self { $this->aiAnalyse = $aiAnalyse; return $this; }
    public function getAiScore(): int { return $this->aiScore; }
    public function setAiScore(int $aiScore): self { $this->aiScore = $aiScore; return $this; }
}
